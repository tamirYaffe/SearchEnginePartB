package SearchEngineTools;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a city index posting list entry.
 */
public class CityPostingEntry {
    /**
     *  docID- the entry docId the city appeared.
     *  positions- list of positions in which the city appeared.
     */
    private int docID;
    private List<String> positions;

    /**
     * default constructor for class parameters
     * @param docID-the entry docId the city appeared.
     * @param positions-list of positions in which the city appeared.
     */
    public CityPostingEntry(int docID, List<Integer>positions){
        this.docID=docID;
        this.positions=new ArrayList<>();
        for(int position:positions){
            this.positions.add(0,""+position);
        }
    }

    @Override
    public String toString() {
        String toString=docID+":";
        for(String position:positions)
            toString+=" "+position;
        return toString;
    }

}
