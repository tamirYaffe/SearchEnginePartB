package SearchEngineTools.ParsingTools.TokenList;


import SearchEngineTools.ParsingTools.Token;
import SearchEngineTools.datamuse.DatamuseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Gets related  words as needed
 * Extends text token list (see part A)
 */
public class QueryTokenList extends TextTokenList {

    /**
     * Words that found related words for in query
     */
   private Collection<Token> foundRelatedWordsFor = new HashSet<>();
    /**
     * related words found
     */
   private List<Token> relatedWords = new ArrayList<>();
    /**
     * if true, tokenlist spellchecks, if false then it doesn't
     */
   private boolean spellCheck;
    /**
     * maximum amounts of synonyms to find for each word in the query
     */
   private int maxSynonyms;
   private DatamuseQuery datamuseQuery = new DatamuseQuery();

    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy, Collection<String> stopWords, boolean spellCheck,int maxSynonyms) {
        clear();
        this.maxSynonyms = maxSynonyms;
        this.spellCheck=spellCheck;
        super.initialize(documentLines,currencySymbols,delimitersToSplitWordBy,stopWords);
    }

    public void initialize(List<String> documentLines, Collection<Character> currencySymbols, Collection<Character> delimitersToSplitWordBy, Collection<String> stopWords){
        initialize(documentLines,currencySymbols,delimitersToSplitWordBy,stopWords,false,0);
    }

    /**
     * sets next token. if needed adds synonyms and similarly spelled words to related word
     */
    protected void setNext(){
        super.setNext();
        Token next = getNext();
        if(next!=null) {
            if(!foundRelatedWordsFor.contains(next)){
                if(spellCheck)
                    spellCheck(next);
                if(maxSynonyms>0)
                    addSynonyms(next);
            }
        }
    }

    /**
     * gets all related words
     * @return related words
     */
    public List<Token> getRelatedWords(){return relatedWords;}

    /**
     * adds synonyms to relatedWords
     * @param token to get synonyms of
     */
    private void addSynonyms(Token token) {
        if(foundRelatedWordsFor.contains(token) || Character.isUpperCase(token.getTokenString().charAt(0)))
            return;
        String tokenString = token.getTokenString();
        List<String> synonyms = datamuseQuery.synonyms(tokenString,maxSynonyms);
        for (int i = 0; i < synonyms.size(); i++) {
            Token t = new Token(synonyms.get(i),-1);
            relatedWords.add(t);
        }
        foundRelatedWordsFor.add(token);
    }

    /**
     *
     * @param token
     */
    private void spellCheck(Token token) {
        String tokenString = token.getTokenString();
        List<String> spelledLikeList = datamuseQuery.spelledSimilar(tokenString,1);
        String spelledLikeString = spelledLikeList.remove(0);
        if(!tokenString.toLowerCase().equals(spelledLikeString)){
            if(Character.isUpperCase(tokenString.charAt(0)))
                spelledLikeString = spelledLikeString.toUpperCase();
            Token spelledLikeToken = new Token(spelledLikeString,-1);
            relatedWords.add(spelledLikeToken);
            if(maxSynonyms>0)
                addSynonyms(token);
        }
    }


}
