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

}
