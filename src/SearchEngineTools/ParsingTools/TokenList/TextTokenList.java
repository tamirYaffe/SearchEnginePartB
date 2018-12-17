package SearchEngineTools.ParsingTools.TokenList;

public class TextTokenList extends DocumentTokenList {


    public TextTokenList(){
        super();
    }
    @Override
    protected String getNextTextLine(){
        currentLine = documentLines.isEmpty() ? null : documentLines.remove(0);
        return currentLine;
    }

}
