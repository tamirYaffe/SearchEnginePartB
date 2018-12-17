package SearchEngineTools;

/**
 * A class that represents a city index posting list entry.
 */
public class PostingEntry {
    /**
     * docID- the entry docId the term appeared.
     * termTF- the term tf in the entry doc.
     * sizeInBytes- the entry size in bytes as it would be written as a string to txt file.
     */
    private int docID;
    private int termTF;
    private int sizeInBytes;

    /**
     * A constructor.
     * @param docID- the entry docId the term appeared.
     * @param termTF- the term tf in the entry doc.
     */
    public PostingEntry(int docID, int termTF) {
        this.docID = docID;
        this.termTF = termTF;
        String docIDS=""+docID;
        String termTFS=""+termTF;
        sizeInBytes+=docIDS.length()+termTFS.length()+2;
    }

    @Override
    public String toString() {
        return docID+" "+termTF;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PostingEntry))
            return false;
        PostingEntry other= (PostingEntry) obj;
        return this.docID==other.docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    /**
     * Comparing two posting entries by their docID.
     * @param other- the other posting entry to compare to this.
     * @return compare result
     */
    public int compareTo(PostingEntry other){
        return this.docID-other.docID;
    }

    //<editor-fold desc="Getters">
    public int getSizeInBytes() {
        return sizeInBytes;
    }

    public int getDocID() {
        return docID;
    }

    public int getTermTF() {
        return termTF;
    }
    //</editor-fold>

}
