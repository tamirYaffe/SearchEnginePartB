package SearchEngineTools;

import javafx.util.Pair;

import java.util.Comparator;

/**
 * A class that compare two posting lists.
 */
public class PostingListComparator implements Comparator<Pair<String, Integer>> {
    @Override
    public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
        String postingList1=o1.getKey();
        String postingList2=o2.getKey();
        int compareResult=postingList1.substring(0,postingList1.indexOf(";")).compareTo(postingList2.substring(0,postingList2.indexOf(";")));
        //return compareResult;
        if(compareResult==0){
            postingList1=postingList1.substring(postingList1.indexOf(";")+1);
            postingList2=postingList2.substring(postingList2.indexOf(";")+1);
            int firstDocID1=Integer.parseInt(postingList1.substring(0,postingList1.indexOf(" ")));
            int firstDocID2=Integer.parseInt(postingList2.substring(0,postingList2.indexOf(" ")));
            return firstDocID1-firstDocID2;
        }
         else
             return compareResult;
    }
}
