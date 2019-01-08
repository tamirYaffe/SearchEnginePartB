package SearchEngineTools.Ranker;

import SearchEngineTools.Document;

import java.util.Comparator;

/**
 * A comparator for min priority queue of documents.
 */
public class MinRankedDocumentsComparator implements Comparator<Document> {
    @Override
    public int compare(Document o1, Document o2) {
        double compareResult=o2.getDocRank()-o1.getDocRank();
        if(compareResult<0)
            return 1;
        if(compareResult>0)
            return -1;
        return 0;
    }
}
