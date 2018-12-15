package SearchEngineTools.ParsingTools.Term;


import SearchEngineTools.ParsingTools.Token;

public class WordTerm extends ATerm {

    public WordTerm(Token token){
        this(token.getTokenString());
        this.addPosition(token.getPosition());
    }

    /**
     * Construct WordTerm from string
     * @param term
     */
    protected WordTerm(String term){
        this.term=term;
        removePunctuation();
        isNumber=false;
    }

    @Override
    protected String createTerm() {
        return term;
    }

    /**
     * Set the term for this string
     * @param term
     */
    public void setTerm(String term){
        this.term=term;
        removePunctuation();
    }

    /**
     * Set term to all lower case letters
     */
    public void toLowerCase(){
        this.term = this.term.toLowerCase();
    }

    /**
     * Set term to all upper case letters
     */
    public void toUperCase(){
        this.term = this.term.toUpperCase();
    }


    /**
     * remove punctuation
     */
    private void removePunctuation(){
        if(term==null || term.length()<=1)
            return;
        int beginIndex = 0;
        int endIndex = term.length()-1;
        boolean isRelevantBeginning = false;
        boolean isRelevantEnd = false;

        //find relevant beginning and end indexes
        while (beginIndex< term.length() && !isRelevantBeginning){
            if(Character.isDigit(term.charAt(beginIndex)) || Character.isLetter(term.charAt(beginIndex)))
                isRelevantBeginning = true;
            else
                beginIndex++;
        }

        while (endIndex >= beginIndex && !isRelevantEnd){
            if(Character.isDigit(term.charAt(beginIndex)) || Character.isLetter(term.charAt(beginIndex)))
                isRelevantEnd = true;
            else
                endIndex--;
        }

        term = term.substring(beginIndex,endIndex+1);
    }
}
