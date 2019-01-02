package SearchEngineTools.ParsingTools;


import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.CityTerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;

import java.util.*;

public class ParseWithStemming extends Parse {

    private Stemmer stemmer;

    /**
     * Constructor for the ParseWithStemming Class.
     * Parse that stems words before returning them.
     */
    public ParseWithStemming(){
        super();
        stemmer = new Stemmer();
    }


    /**
     * returns final collection from list
     * @param
     * @return
     */
    /*protected Collection<ATerm> getFinalTermCollection(Map<String, ATerm> occurrencesOfTerms) {
        ArrayList<ATerm> toReturn = new ArrayList<>(occurrencesOfTerms.size());
        HashMap<String, WordTerm> toStem= new HashMap<String, WordTerm>();
        for (String termString:occurrencesOfTerms.keySet()) {
            ATerm term = occurrencesOfTerms.get(termString);
            if(term instanceof WordTerm && !(term instanceof CityTerm)){
                toStem.put(term.getTerm(), (WordTerm) term);
            }
            else
                toReturn.add(term);
        }
        toReturn.addAll(getStemmedWords(toStem));
        return toReturn;
    }

    private Collection<ATerm> getStemmedWords(Map<String, WordTerm> toStem) {
        HashMap<String, WordTerm> stemmed = new HashMap<>(toStem.size());

        //stem terms and ad them to map
        for (String s:toStem.keySet()) {
            boolean isUpperCase = Character.isUpperCase(s.charAt(0));
            WordTerm term = toStem.get(s);
            String stemmedString = s.toLowerCase();
            stemmedString = stemmer.stem(stemmedString);
            //if stemmed words contains stemmed lower case, add positions and occurrences
            if(stemmed.containsKey(stemmedString)){
                WordTerm other = stemmed.get(stemmedString);
                other.addPositions(term);
                int newOccurrences = other.getOccurrences()+term.getOccurrences();
                other.setOccurrences(newOccurrences);
                continue;
            }
            //if stemmed words contains stemmed upper case
            String upperCaseStemmed = stemmedString.toUpperCase();
            if(stemmed.containsKey(upperCaseStemmed)){
                //add positions and occurrences
                if(isUpperCase){
                    WordTerm other = stemmed.get(upperCaseStemmed);
                    other.addPositions(term);
                    int newOccurrences = other.getOccurrences()+term.getOccurrences();
                    other.setOccurrences(newOccurrences);
                }
                //remove upper case, add positions and occurrences, insert lowercase
                else {
                    WordTerm other = stemmed.remove(upperCaseStemmed);
                    term.addPositions(term);
                    int newOccurrences = other.getOccurrences()+term.getOccurrences();
                    term.setOccurrences(newOccurrences);
                    term.setTerm(stemmedString);
                    stemmed.put(stemmedString,term);
                }
                continue;
            }
            //stemmed does not contain word, add it
            if(isUpperCase)
                term.setTerm(upperCaseStemmed);
            else
                term.setTerm(stemmedString);
            stemmed.put(term.getTerm(),term);
        }

        ArrayList stemmedTerms = new ArrayList(stemmed.size());
        for (String s:stemmed.keySet()){
            stemmedTerms.add(stemmed.get(s));
        }
        return stemmedTerms;
    }


    /**
     *Constructor for the ParseWithStemming Class.
     *Parse that stems words before returning them.
     * @param stopWords words to ignore when stemming
     */
    public ParseWithStemming(List<String> stopWords){
        super(stopWords);
        stemmer = new Stemmer();
    }


    protected WordTerm createWordTerm(Token token) {
        String tokenString = token.getTokenString();
        boolean isStopWord = isStopWord(tokenString);
        if(isStopWord)
            return null;
        boolean isUpperCase = Character.isUpperCase(tokenString.charAt(0));
        tokenString = tokenString.toLowerCase();
        tokenString = stemmer.stem(tokenString);
        if(isUpperCase)
            token.setTokenString(tokenString.toUpperCase());
        else
            token.setTokenString(tokenString);
        return new WordTerm(token);
    }

    /*public Collection<ATerm> parseDocument(List<String> document){
        Map<ATerm,Integer> occurrencesOfTerms = new HashMap<>();
        List<ATerm> terms=new ArrayList<>();
        List<String> tokens=tokenize(document);
        //do text operations(remove unnecessary chars, change words by the assignment rules).
        //removeUnnecessaryChars(tokens);
        //get terms
        List<ATerm> next = null;
        do{
            next = getNextTerm(tokens);
            if(next!=null) {
                //allow stemming.
                handleAll(next, occurrencesOfTerms);
            }
        }while (next != null);

        return getFinalList(occurrencesOfTerms);
    }

    private void handleAll(List<ATerm> toHandle, Map<ATerm,Integer> occurances) {
        for (ATerm term:toHandle) {
            if (term instanceof WordTerm){
                //remove stop words
                boolean isLowerCase = isLowerCase((WordTerm) term);
                if(this.stopWords.contains(term.getTerm())){
                    //remove term and continue
                    toHandle.remove(term);
                    continue;
                }
                //stem
                stemTerm((WordTerm) term);
                //add to occurrance list
                addWordTermToList((WordTerm) term,occurances, isLowerCase);
            }
            else
                addToOccurancesList(term,occurances);
        }

    }

    private void stemTerm(WordTerm term){
        (term).toLowerCase();
        (term).setTerm(stemmer.stem(term.getTerm()));
    }

    private boolean isLowerCase(WordTerm term){
        if(Character.isLetter(term.getTerm().charAt(0)) && Character.isLowerCase(term.getTerm().charAt(0)))
            return true;
        return false;
    }

    protected Collection<ATerm> getFinalList(Map<ATerm,Integer> occurrances){
        Collection<ATerm> finalList = occurrances.keySet();
        for (ATerm term:finalList) {
            term.setOccurrences(occurrances.get(term));
        }
        return finalList;
    }*/
}
