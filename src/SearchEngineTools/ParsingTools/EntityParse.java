package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.*;
import SearchEngineTools.ParsingTools.TokenList.ITokenList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityParse extends Parse {



    @Override
    protected Collection<ATerm> getFinalTermCollection(Map<String, ATerm> occurrencesOfTerms) {
        ArrayList<ATerm> toReturn = new ArrayList<>(occurrencesOfTerms.size());
        for (String termString:occurrencesOfTerms.keySet()) {
            ATerm term = occurrencesOfTerms.get(termString);
            if(Character.isUpperCase(termString.charAt(0)))
                toReturn.add(term);
        }
        return toReturn;
    }
}


