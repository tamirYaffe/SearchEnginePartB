package SearchEngineTools.ParsingTools.TokenList;


import SearchEngineTools.ParsingTools.Term.CityTerm;
import SearchEngineTools.ParsingTools.Token;
import eu.fayder.restcountries.v1.domain.Country;
import eu.fayder.restcountries.v1.rest.CountryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentTokenList extends TextTokenList {



    private boolean isText;
    private CityTerm cityTerm;
    private CountryService countryService = CountryService.getInstance();
    private String docLanguage = null;


    /**
     * InitializeDocumentTokenList
     * @param documentLines
     * @param currencySymbols
     * @param delimitersToSplitWordBy
     * @param stopWords
     */
    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy, Collection<String> stopWords) {
        isText=false;
        super.initialize(documentLines,currencySymbols,delimitersToSplitWordBy,stopWords);
    }

        public String getDocLanguage(){
        return docLanguage;
    }
    /**
     * Constructer for the Document TokenList Class
     */






    /**
     * gets the next text line, sets cityTerm
     * @return
     */
    protected String getNextTextLine() {
        if(isText){
            currentLine = documentLines.remove(0);
            if(currentLine.contains("<F P=104>")) {
                extractCityTerm(currentLine);
                return getNextTextLine();
            }
            if(currentLine.contains("<F P=105>")) {
                setDocLanguage(currentLine);
                return getNextTextLine();
            }
            if((currentLine.contains("<F P=") && currentLine.contains("</F>"))|| currentLine.contains("Article Type:")) {
                return getNextTextLine();
            }
            if(currentLine.equals("</TEXT>")){
                isText = false;
                String nextLine = getNextTextLine();
                return nextLine;
            }
            else
                return currentLine;

            //find first actual line of text
        }
        else {
            while (!documentLines.isEmpty() && !isText){
                currentLine = documentLines.remove(0);
                if(currentLine!=null && currentLine.contains("Language: <F P=105>")){
                    setDocLanguage(currentLine);
                    return getNextTextLine();
                }
                if(currentLine.contains("<F P=104>")) {
                    extractCityTerm(currentLine);
                    continue;
                }
                if(currentLine.equals("<TEXT>")) {
                    isText = true;
                    return getNextTextLine();
                }
            }
        }
        return null;
    }

    /**
     * extracts cityTerm frim line with appropriate tag
     * @param currentLine
     */
    private void extractCityTerm(String currentLine) {
        String cityName = null;
        boolean foundCity = false;
        Country country = null;
        List<String> cityNameWords = getLongestCityNameCandidate(currentLine,1);
        if(cityNameWords == null)
            return;
        for (int i = cityNameWords.size()-1; i >= 0 && !foundCity; i--) {
            cityName = "";
            for (int j = 0; j <= i; j++) {
                if(j!=0)
                    cityName+=" ";
                cityName+=cityNameWords.get(j);
            }
            List<Country> countryCandidate = countryService.getByCapital(cityName);
            if(countryCandidate!=null && !countryCandidate.isEmpty()){
                country = countryCandidate.get(0);
                foundCity = true;
            }
        }
        if(foundCity){
            cityTerm = new CityTerm(cityName.toUpperCase(),country);
        }
    }

    private List<String> getLongestCityNameCandidate(String cityLine, int maxWordsInName){
        String lineWithoutTag = currentLine.length()>=10 ? currentLine.substring(9) : null;
        if(lineWithoutTag==null)
            return null;
        String[] splitLineWithoutTag = lineWithoutTag.split(" ");
        List<String> cityName = new ArrayList<>(maxWordsInName);
        int words = 0;
        for (int i = 0; i < splitLineWithoutTag.length && words<maxWordsInName; i++) {
            String currentIndexString = splitLineWithoutTag[i];
            if(currentIndexString!=null && currentIndexString.length()!=0 && !currentIndexString.equals("</F>")){
                cityName.add(currentIndexString);
                words++;
            }
        }
        return cityName;
    }



    private void setDocLanguage(String currentLine){
        currentLine = currentLine.substring(19);
        int indexOfTag = currentLine.indexOf(("</F>"));
        indexOfTag = indexOfTag==-1 ? currentLine.length()-1 : indexOfTag;
        try {
            if (currentLine.length() == 0) {
                return;
            }
            currentLine = currentLine.substring(0, indexOfTag);

        }
        catch (Exception e){
            System.out.println("error:"+currentLine+indexOfTag);;
        }
        currentLine = removeUnnecessaryChars(currentLine);
        docLanguage = currentLine;
    }



    public CityTerm getCityTerm() {
        return cityTerm;
    }


}
