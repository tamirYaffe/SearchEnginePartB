package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;

import java.util.Collection;
import java.util.List;

public class TextTokenList extends ATokenList {

    private int wordCount;
    protected String currentLine;
    protected List<String> documentLines;
    private Collection<String> stopWords;
    Collection<Character> delimitersToSplitWordBy;
    Collection<Character> currencySymbols;

    public TextTokenList(){
        super();
    }


    protected String getNextTextLine(){
        currentLine = documentLines.isEmpty() ? null : documentLines.remove(0);
        return currentLine;
    }

    /**
     * get the next token from the current document
     * @return
     */
    protected Token getNextToken(){
        Token token=null;
        //get next line
        if(currentLine == null){
            currentLine = getNextTextLine();
            if(currentLine==null)
                return null;
        }
        while (currentLine!=null && token==null){
            token = getNextTokenFromCurrentLine();
        }
        return token;
    }

    /**
     * Gets the next token from current line
     * @return
     */
    private Token getNextTokenFromCurrentLine() {
        int indexOfFirstSpace = currentLine.indexOf(' ');
        String tokenString;
        //no space
        if(indexOfFirstSpace==-1){
            tokenString = removeUnnecessaryChars(currentLine);
            currentLine = getNextTextLine();
        }
        else {
            tokenString = removeUnnecessaryChars(currentLine.substring(0,indexOfFirstSpace));
            currentLine = indexOfFirstSpace>=currentLine.length() ? getNextTextLine() : currentLine.substring(indexOfFirstSpace+1);
        }
        Token toReturn = null;
        if(tokenString!=null){
            tokenString = (tokenString!=null && !stopWords.contains(tokenString.toLowerCase())) ? tokenString : null;
            toReturn = tokenString!=null ?new Token(tokenString,wordCount) : null;
            wordCount++;
        }
        return toReturn;
    }

    /**
     * checks that all Tokens in list are valid, removes invalid Tokens
     * @param tokens
     */
    protected void validateTokensList(List<Token> tokens){
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String tokenString = token.getTokenString();
            tokenString = removeUnnecessaryChars(tokenString);
            if(tokenString==null || tokenString.length()==0){
                tokens.remove(i);
                i--;
            }
            else {
                token.setTokenString(tokenString);
            }
        }
    }

    /**
     * removes unnecessary characters from beggining and end of string
     * @param sToken
     * @return
     */
    protected String removeUnnecessaryChars(String sToken) {
        if(sToken==null || sToken.equals(""))
            return null;
        int firstNecessary = 0;
        int lastNecessary = sToken.length()-1;
        //find first necessary index
        boolean foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary))
                || (sToken.length()>1 && currencySymbols.contains(sToken.charAt(firstNecessary)) && Character.isDigit(sToken.charAt(firstNecessary+1)))
                || (sToken.length()==1 && '%'==sToken.charAt(0)));
        while (!foundFirstIndex && firstNecessary<sToken.length()){
            foundFirstIndex = (Character.isDigit(sToken.charAt(firstNecessary)) || Character.isLetter(sToken.charAt(firstNecessary)))||
                    (firstNecessary>sToken.length()-1 && delimitersToSplitWordBy.contains(sToken.charAt(firstNecessary)) && Character.isDigit(sToken.charAt(firstNecessary+1)));
            if(!foundFirstIndex)
                firstNecessary++;
        }
        if(firstNecessary>lastNecessary)
            return null;
        while (lastNecessary>0 &&
                !(Character.isDigit(sToken.charAt(lastNecessary-1))&& sToken.charAt(lastNecessary)=='%')&&
                !(Character.isDigit(sToken.charAt(lastNecessary)) ||//first digit is not digit
                        Character.isLetter(sToken.charAt(lastNecessary)) ||//first digit is not letter
                        currencySymbols.contains(""+sToken.charAt(lastNecessary)))){ //first digit is not currency
            lastNecessary--;
        }
        if(firstNecessary>lastNecessary)
            return null;
        if(firstNecessary!=0 || lastNecessary!=sToken.length()-1)
            sToken = sToken.substring(firstNecessary,lastNecessary+1);
        if(sToken.length()>=2 && sToken.substring(sToken.length()-2,sToken.length()).equals("'s"))
            sToken = sToken.substring(0,sToken.length()-2);
        sToken = sToken.length()>0 ? sToken : null;
        return sToken;

    }

    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy,Collection<String> stopWords) {
        this.documentLines=documentLines;
        this.currencySymbols=currencySymbols;
        this.delimitersToSplitWordBy=delimitersToSplitWordBy;
        this.stopWords=stopWords;
        setNext();
    }

    @Override
    public void clear() {
        super.clear();
        this.currentLine=null;
        this.wordCount=0;
        this.documentLines=null;
    }

}
