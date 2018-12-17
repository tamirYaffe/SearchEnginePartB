package SearchEngineTools;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a posting list by gaps between docId's.
 */
public class PostingList {
    private List<PostingEntry> postingList;
    private int lastDocId;

    //<editor-fold desc="Constructors">
    /**
     * Default constructor.
     */
    public PostingList() {
        postingList=new ArrayList<>();
    }

    /**
     * creates a posting list represented by docID (not gaps).
     * @param postingList- posting list represented in string.
     */
    public PostingList(String postingList){
        this.postingList=new ArrayList<>();
        postingList=postingList.substring(postingList.indexOf(";")+1);
        String[] splitPostingList=postingList.split(" ");
        for (int i = 0; i < splitPostingList.length-1; i+=2){
            int currDocID=Integer.parseInt(splitPostingList[i]);
            lastDocId+=currDocID;
            this.postingList.add(new PostingEntry(lastDocId,Integer.parseInt(splitPostingList[i+1])));
        }
    }
    //</editor-fold>

    /**
     * A static function that calculates and returns Last DocID in the input posting list.
     * @param postingList- list to calculate on.
     * @return- the Last DocID in the input posting list.
     */
    public static int calculateLastDocID(String postingList) {
        String[] splitPostingList=postingList.split(" ");
        int lastDocId=0;
        for (int i = 0; i < splitPostingList.length-1; i+=2){
            int currDocID= 0;
            try {
                currDocID = Integer.parseInt(splitPostingList[i]);
            } catch (NumberFormatException e) {
                System.out.println(splitPostingList[i]);
            }
            lastDocId+=currDocID;
        }
        return lastDocId;
    }


    /**
     * Adds input posting entry to the posting list, and returns the difference from previous size.
     * @param postingEntry- posting entry to add.
     * @return - the difference from previous size.
     */
    public int add(PostingEntry postingEntry) {
        if(postingList.isEmpty()){
            postingList.add(postingEntry);
            lastDocId=postingEntry.getDocID();
            return postingEntry.getSizeInBytes();
        }
        else {
            PostingEntry gapPostingEntry;
            int docId = postingEntry.getDocID();
            int termTF = postingEntry.getTermTF();

            if(docId>lastDocId){
                gapPostingEntry=new PostingEntry(docId-lastDocId,termTF);
                postingList.add(gapPostingEntry);
                lastDocId=docId;
                return gapPostingEntry.getSizeInBytes();
            }
            else{//docId<lastDocId
                int currDocID=0;
                int prevDocID=0;
                PostingEntry currPostingEntry;
                for (int i = 0; i < postingList.size(); i++) {
                    currPostingEntry=postingList.get(i);
                    prevDocID=currDocID;
                    currDocID+=currPostingEntry.getDocID();
                    if(docId<currDocID){
                        gapPostingEntry=new PostingEntry(docId-prevDocID,termTF);
                        currPostingEntry.setDocID(currDocID-docId);
                        postingList.add(i,gapPostingEntry);
                        return gapPostingEntry.getSizeInBytes();
                    }
                }
            }


        }
        return -1;
    }

    /**
     * Merges two posting lists represented by docID's to merged list represented by gaps.
     * merge done by linear merge.
     *
     * @param list1- list to merge.
     * @param list2- list to merge.
     * @return - the merged list.
     */
    public static PostingList mergeLists(PostingList list1,PostingList list2){
        PostingList mergedList=new PostingList();
        while (list1.postingList.size() > 0 && list2.postingList.size() > 0) {
            if (list1.postingList.get(0).compareTo(list2.postingList.get(0)) < 0) {
                mergedList.add(list1.postingList.get(0));
                list1.postingList.remove(0);
            }
            else {
                mergedList.add(list2.postingList.get(0));
                list2.postingList.remove(0);
            }
        }

        if (list1.postingList.size() > 0) {
            mergedList.addAll(list1.postingList);
        }
        else if (list2.postingList.size() > 0) {
            mergedList.addAll(list2.postingList);
        }
        return mergedList;
    }

    @Override
    public String toString() {
        String s="";
        boolean first=true;
        for (PostingEntry postingEntry : postingList){
            if(first){
                first=false;
                s+=""+postingEntry;
            }
            else
                s+=" " + postingEntry;
        }
        return s;
    }

    private void addAll(List<PostingEntry> other) {
        for(PostingEntry postingEntry:other)
            add(postingEntry);
    }

    /**
     * Only for posting lists created with this(String) which are not represented in gaps.
     * @return
     */
    public PostingEntry RemoveFirst(){
        if(!postingList.isEmpty())
            return postingList.remove(0);
        return null;
    }
}
