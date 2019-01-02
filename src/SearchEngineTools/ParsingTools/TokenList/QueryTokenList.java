package SearchEngineTools.ParsingTools.TokenList;


import SearchEngineTools.ParsingTools.Token;
import SearchEngineTools.datamuse.DatamuseQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class QueryTokenList extends TextTokenList {

   private Collection<Token> foundRelatedWordsFor = new HashSet<>();
   private List<Token> relatedWords = new ArrayList<>();
   private boolean spellCheck;
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

    public List<Token> getRelatedWords(){return relatedWords;}

    private void addSynonyms(Token token) {
        if(foundRelatedWordsFor.contains(token) || !Character.isUpperCase(token.getTokenString().charAt(0)))
            return;
        String tokenString = token.getTokenString();
        List<String> synonyms = datamuseQuery.synonyms(tokenString,maxSynonyms);
        for (int i = 0; i < synonyms.size(); i++) {
            Token t = new Token(synonyms.get(i),-1);
            relatedWords.add(t);
        }
        foundRelatedWordsFor.add(token);
    }

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
