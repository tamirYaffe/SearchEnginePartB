package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;
import SearchEngineTools.datamuse.DatamuseQuery;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class QueryTokenList extends TextTokenList{


    public QueryTokenList(){
        super();
    }

    @Override
    protected String getNextTextLine() {
        while (!documentLines.isEmpty()){
            currentLine = documentLines.remove(0);
            if(currentLine.contains("<title>")){
                currentLine = currentLine.substring(7);
                return currentLine;
            }
        }
        return null;
    }
}
