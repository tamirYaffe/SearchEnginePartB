package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;

import java.util.ArrayList;
import java.util.List;


public abstract class ATokenList implements ITokenList {


    private List<Token> prepended;
    private List<Token> appended;
    private Token next;

    ATokenList(){
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
            next = prepended.remove(0);
        }
        else {
            next = getNextToken();
            if(next == null){
                next = appended.isEmpty() ? null : appended.remove(0);
            }
        }

    }

    protected abstract Token getNextToken();

    @Override
    public void prepend(List<Token> tokens) {
        validateTokensList(tokens);
        prependValidTokens(tokens);
    }

    protected abstract void validateTokensList(List<Token> tokens);

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

    @Override
    public Token get(int index) {
        if(index==0) {
            return peek();
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

    @Override
    public boolean has(int index) {
        try {
            get(index);
            return true;
        }catch (NullPointerException e){
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return next==null;
    }

    public void clear(){
        this.prepended.clear();
        this.appended.clear();
        this.next=null;
    }

    protected Token getNext(){
        return next;
    }

    protected void setNext(Token token){
        next = token;
    }
}
