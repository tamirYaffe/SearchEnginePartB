package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ATokenList implements ITokenList {


    private List<Token> prepended;
    private List<Token> appended;
    protected Token next;
    protected String currentLine;
    private int wordCount;

    public ATokenList(){
        appended = new ArrayList<>();
        prepended = new ArrayList<>();
    }

    @Override
    public Token peek() {
        if(isEmpty())
            throw new NullPointerException();
        return next;
    }

    @Override
    public Token pop() {
        if(isEmpty())
            throw new NullPointerException();
        Token token = next;
        setNext();
        return token;
    }

    /**
     * set the next term
     */
    protected void setNext(){
        if(!prepended.isEmpty()){
            next = getNextTokenFromPrepended();
        }
        else {
            next = getNextToken();
            if(next == null){
                next = appended.isEmpty() ? null : appended.remove(0);
            }
        }
    }

    protected Token getNextTokenFromPrepended(){
        return prepended.remove(0);
    }

    @Override
    public void prepend(List<Token> tokens) {
        validateTokensList(tokens);
        prependValidTokens(tokens);
    }

    public void prependValidTokens(List<Token> tokens){
        if(next!=null)
            tokens.add(next);
        next = !tokens.isEmpty() ? tokens.remove(0) : next;
        prepended.addAll(0,tokens);
    }

    @Override
    public void append(List<Token> tokens) {
        validateTokensList(tokens);
        appended.addAll(tokens);
    }

    /**
     * checks that all Tokens in list are valid, removes invalid Tokens
     * @param tokens
     */
    protected abstract void validateTokensList(List<Token> tokens);

    public void clear(){
        this.prepended.clear();
        this.appended.clear();
        this.next=null;
    }

    @Override
    public boolean isEmpty() {
        return next==null;
    }

    @Override
    public Token get(int index) {
        if(index==0) {
            try {
                return peek();
            }
            catch (Exception e){
                return null;
            }
        }
        int amountOfTokensToAddToPrepended = index-prepended.size();
        List<Token> toPrepend = new ArrayList<>(amountOfTokensToAddToPrepended);
        while (amountOfTokensToAddToPrepended > 0){
            Token token = pop();
            toPrepend.add(token);
        }
        prepended.addAll(toPrepend);
        return prepended.get(index-1);
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
        Collection<String> stopWords = getStopWords();
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
        if(currentLine==null || currentLine.length()==0)
            currentLine = getNextTextLine();
        return toReturn;
    }

    /**
     * removes unnecessary characters from beggining and end of string
     * @param sToken
     * @return
     */
    protected String removeUnnecessaryChars(String sToken) {
        Collection<Character> delimitersToSplitWordBy = getDelimitersToSplitWordBy();
        Collection<Character> currencySymbols = getCurrencySymbols();
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

    protected abstract String getNextTextLine();

    protected abstract Collection<String> getStopWords();

    protected abstract Collection<Character> getDelimitersToSplitWordBy();

    protected abstract Collection<Character> getCurrencySymbols();

    @Override
    public boolean has(int index) {
        return get(index)!=null;
    }
}
