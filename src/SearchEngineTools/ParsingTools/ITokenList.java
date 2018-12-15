package SearchEngineTools.ParsingTools;

import java.util.List;

public interface ITokenList {

    /**
     * get next token withot removing it from list
     * @return
     */
    Token peek();

    /**
     * get next token from list and remove it
     * @return
     */
    Token pop();

    /**
     * Add list of tokens to the begining of token list
     * @param tokens
     */
    void prepend(List<Token> tokens);

    /**
     * add list of tokens to end of list
     * @param tokens
     */
    void append(List<Token> tokens);

    /**
     * clear token list
     */
    void clear();

    /**
     *
     * @return true if list list empty, false otherwise
     */
    boolean isEmpty();

    /**
     * get token at specified index
     * @param index
     * @return
     */
    Token get(int index);

    /**
     * check if list contains token at specified index
     * @param index
     * @return true if contains Token at specified index, false otherwise
     */
    boolean has(int index);

    /**
     * Add Valid tokens to list. Will not check to see if tokens contain value words
     * @param toPrepend
     */
    void prependValidTokens(List<Token> toPrepend);
}
