package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;

import java.util.Collection;
import java.util.List;

public class TextTokenList extends ATokenList {



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

    @Override
    protected Collection<String> getStopWords() {
        return stopWords;
    }

    @Override
    protected Collection<Character> getDelimitersToSplitWordBy() {
        return delimitersToSplitWordBy;
    }

    @Override
    protected Collection<Character> getCurrencySymbols() {
        return currencySymbols;
    }


    /**
     * InitializeDocumentTokenList
     * @param documentLines
     * @param currencySymbols
     * @param delimitersToSplitWordBy
     * @param stopWords
     */
    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy, Collection<String> stopWords) {
        this.documentLines=documentLines;
        this.currencySymbols=currencySymbols;
        this.delimitersToSplitWordBy=delimitersToSplitWordBy;
        this.stopWords=stopWords;
        setNext();
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

}
