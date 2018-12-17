package SearchEngineTools.ParsingTools.TokenList;

import SearchEngineTools.ParsingTools.Token;

import java.util.List;

public class QueryTokenList extends DocumentTokenList{

    @Override
    protected String getNextTextLine() {
        while (!documentLines.isEmpty()) {
            currentLine = documentLines.remove(0);
            if (currentLine.contains("<title> dsfdsgfds")) {
                currentLine = currentLine.substring(7);
                return currentLine;
            }
        }
        return null;
    }
}
