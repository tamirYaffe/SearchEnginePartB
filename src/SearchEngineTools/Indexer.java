package SearchEngineTools;


import javafx.util.Pair;

import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.CityTerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that represents the indexer process.
 */
public class Indexer {
    //dictionary that holds term->df. used also for word check for big first word.
    private Map<String, Pair<Integer, Integer>> dictionary;

    //dictionary and posting list in one hash.
    private Map<String, PostingList> tempInvertedIndex;

    //dictionary and posting list in one hash for cities.
    private Map<String, Pair<CityTerm, List<CityPostingEntry>>> cityInvertedIndex;

    //temp posting blocks to write size.
    private int memoryBlockSize;

    //current block file memory size
    private int usedMemory;

    //posting files path.
    private String postingFilesPath;

    //var for determine if to use stemming in the parse.
    private boolean useStemming;

    //file Separator "/" in unix or "\" in windows.
    private String fileSeparator = System.getProperty("file.separator");


    //concurrent vars.
    private AtomicInteger blockNum = new AtomicInteger();

    //<editor-fold desc="Constructors">
    /**
     * A default constructor.
     */
    public Indexer() {
        dictionary = new LinkedHashMap<>();
        tempInvertedIndex = new HashMap<>();
        cityInvertedIndex = new LinkedHashMap<>();
    }

    public Indexer(int memoryBlockSize, String postingFilesPath) {
        this();
        this.memoryBlockSize = memoryBlockSize;
        this.postingFilesPath = postingFilesPath;
    }

    public Indexer(int memoryBlockSize) {
        this();
        this.memoryBlockSize = memoryBlockSize;
    }
    //</editor-fold>

    /**
     * Creates the dictionary and posting files.
     *
     * @param terms - list of the document terms(after parse).
     * @param document - the document object of the terms list.
     */
    public void createInvertedIndex(Iterator<ATerm> terms, Document document) {
        while (terms.hasNext()) {
            ATerm aTerm = terms.next();
            if (aTerm.getTerm().equals("."))
                continue;
            if (aTerm instanceof WordTerm && Character.isLetter(aTerm.getTerm().charAt(0)))
                handleCapitalWord(aTerm);
            String term = aTerm.getTerm();
            if (aTerm instanceof CityTerm) {
                document.setDocCity(term);
                addToCityIndex(aTerm, document.getDocID());
                if(aTerm.getOccurrences()==0)
                    continue;
            }
            int termOccurrences = aTerm.getOccurrences();
            document.updateDocInfo(termOccurrences);

            //add or update dictionary.
            if (!dictionary.containsKey(term)) {
                dictionary.put(term, new Pair<>(1, -1));
            } else
                dictionary.replace(term, new Pair<>(dictionary.get(term).getKey() + 1, -1));

            //add or update temp inverted index.
            PostingList postingsList;
            PostingEntry postingEntry = new PostingEntry(document.getDocID(), termOccurrences);
            if (!tempInvertedIndex.containsKey(term)) {
                postingsList = new PostingList();
                tempInvertedIndex.put(term, postingsList);
                usedMemory += term.length() + 1;
            } else {
                postingsList = tempInvertedIndex.get(term);
            }
            int addedMemory = postingsList.add(postingEntry);

            //update usedMemory .
            if (addedMemory != -1)
                usedMemory += addedMemory;

            //check and write to disk if needed.
            if (usedMemory > memoryBlockSize) {
                try {
                    sortAndWriteInvertedIndexToDisk();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //init dictionary and posting lists.
                tempInvertedIndex.clear();
                usedMemory = 0;
            }
        }

        //write current document info to disk.
        document.writeDocInfoToDisk(postingFilesPath);
    }

    /**
     * Merging all posting blocks into one file(postingLists).
     * @throws IOException
     */
    public void mergeBlocks() throws IOException {
        System.out.println("starting merge");
        System.out.println("dictionary size: " + getDictionarySize());

        //init vars.
        int postingListIndex = 0;
        BufferedReader[] readers = new BufferedReader[blockNum.get()];

        //priority queue for posting lists that sorts by posting list term and docID in the list.
        PostingListComparator comparator = new PostingListComparator();
        PriorityQueue<Pair<String, Integer>> queue = new PriorityQueue<>(comparator);

        String curPostingList;
        List<String> bufferPostingLists = new ArrayList<>();

        //open BufferedWriter.
        String fileName;
        if(useStemming)
            fileName="postingListsStemming.txt";
        else
            fileName="postingLists.txt";
        String pathName = postingFilesPath + fileSeparator + fileName;
        File file = new File(pathName);
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);


        //create readers and init queue.
        for (int i = 0; i < blockNum.get(); i++) {
            fileName = "blocks" + fileSeparator + "block" + i + ".txt";
            readers[i] = new BufferedReader(new FileReader(fileName));
            queue.add(new Pair<>(readers[i].readLine(), i));
        }
        while (!queue.isEmpty()) {
            curPostingList = getNextPostingList(queue, readers);
            curPostingList = checkForMergeingPostingLines(queue, readers, curPostingList);
            String term = extractTerm(curPostingList);
            dictionary.replace(term, new Pair<>(dictionary.get(term).getKey(), postingListIndex++));

            //write to buffer posting lists
            bufferPostingLists.add(curPostingList);
            usedMemory += curPostingList.length() - term.length();

            //check size of buffer
            if (usedMemory > memoryBlockSize) {
                writeBufferPostingListsToDisk(bw, bufferPostingLists);
                //init dictionary and posting lists.
                bufferPostingLists.clear();
                usedMemory = 0;
            }
        }
        //writing buffer remaining posting lists
        if (usedMemory > 0) {
            writeBufferPostingListsToDisk(bw, bufferPostingLists);
        }
        bw.close();
        sortAndWriteDictionaryToDisk();
        resetIndex();
    }

    /**
     * Sorts and write the temp inverted index to file: block*, *-curr block number.
     * @throws InterruptedException
     */
    public void sortAndWriteInvertedIndexToDisk() throws InterruptedException {
        if (usedMemory == 0)
            return;
        System.out.println("writing to disk: blockNum" + blockNum.get());
        String fileName = "blocks" + fileSeparator + "block" + blockNum.get() + ".txt";
        blockNum.getAndIncrement();
        try (FileWriter fw = new FileWriter(fileName);
             BufferedWriter bw = new BufferedWriter(fw)) {
            List<String> keys = new ArrayList<>(tempInvertedIndex.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                bw.write(key + ";");
                bw.write("" + tempInvertedIndex.get(key));
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes city index to file cityIndex in disk.
     */
    public void writeCityIndex() {
        String fileName;
        if(useStemming)
            fileName="cityIndexStemming.txt";
        else
            fileName="cityIndex.txt";
        String pathName = postingFilesPath + fileSeparator + fileName;
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            List<String> keys = new ArrayList<>(cityInvertedIndex.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                Pair<CityTerm, List<CityPostingEntry>> indexPair = cityInvertedIndex.get(key);
                CityTerm cityTerm = indexPair.getKey();
                List<CityPostingEntry> postingList = indexPair.getValue();
                String sPostingList = "";
                for (int i = 0; i < postingList.size(); i++) {
                    if (i == 0)
                        sPostingList += postingList.get(i);
                    else
                        sPostingList += "," + postingList.get(i);
                }
                bw.write(key + " " + cityTerm.getCountryCurrency() + " " + cityTerm.getStatePopulation() + " " + sPostingList);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all the class data structures.
     */
    public void clear() {
        dictionary.clear();
        tempInvertedIndex.clear();
        cityInvertedIndex.clear();
    }

    //<editor-fold desc="Private functions">

    /**
     * Extracts term from posting list.
     * @param postingList- list to extract term from.
     * @return -the term from the posting list.
     */
    private String extractTerm(String postingList) {
        return postingList.substring(0, postingList.indexOf(";"));
    }

    /**
     * Writes posting lists in input bufferPostingLists to file postingLists in disk.
     * @param bw- BufferWriter we use to write.
     * @param bufferPostingLists- posting lists to write.
     * @throws IOException
     */
    private void writeBufferPostingListsToDisk(BufferedWriter bw, List<String> bufferPostingLists) throws IOException {

        for (String postingList : bufferPostingLists) {
            bw.write(postingList.substring(postingList.indexOf(";") + 1));
//            bw.write(postingList);
            bw.newLine();
        }
    }


    /**
     * Remove top of priority queue,and add the next line from the removed line block.
     *
     * @param queue - queue to get next posting list from.
     * @param readers - an array of buffered readers to read from blocks if necessary.
     * @return - next posting list from priority queue.
     * @throws IOException
     */
    private String getNextPostingList(PriorityQueue<Pair<String, Integer>> queue, BufferedReader[] readers) throws IOException {
        String postingList;
        while (true) {
            Pair<String, Integer> postingListPair = queue.poll();
            postingList = postingListPair.getKey();
            int blockIndex = postingListPair.getValue();

            String nextPostingList = readers[blockIndex].readLine();
            if (nextPostingList != null)
                queue.add(new Pair<>(nextPostingList, blockIndex));
            //handling words lower/upper case
            String term = extractTerm(postingList);
            if (Character.isUpperCase(term.charAt(0)) && dictionary.containsKey(term.toLowerCase())) {
                //change posting list
                String updatedPostingList = term.toLowerCase() + postingList.substring(postingList.indexOf(";"));
                // add to queue
                queue.add(new Pair<>(updatedPostingList, blockIndex));
            } else
                break;

        }
        return postingList;
    }

    /**
     * Checks if there are posting lists in the Priority Queue that need to be merged.
     * @param queue- the Priority Queue of posting lists.
     * @param readers -an array of buffered readers to read from blocks if necessary.
     * @param curPostingList- current posting list we got from the queue.
     * @return - the currPosting list if there was no merge, otherwise returns the merged list.
     * @throws IOException
     */
    private String checkForMergeingPostingLines(PriorityQueue<Pair<String, Integer>> queue, BufferedReader[] readers, String curPostingList) throws IOException {
        if (queue.isEmpty())
            return curPostingList;
        String nextPostingList = queue.peek().getKey();
        while (extractTerm(curPostingList).equals(extractTerm(nextPostingList))) {
            curPostingList = mergePostingLists(curPostingList, nextPostingList);
            getNextPostingList(queue, readers);
            if (queue.isEmpty())
                break;
            nextPostingList = queue.peek().getKey();
        }
        return curPostingList;
    }

    /**
     * Returns the merged posting list from the two i.nput posting lists.
     * @param postingList1- posting list to merge
     * @param postingList2- posting list to merge.
     * @return - the merged posting list from the two i.nput posting lists.
     */
    private String mergePostingLists(String postingList1, String postingList2) {
        String term = extractTerm(postingList1);
        postingList1 = postingList1.substring(postingList1.indexOf(";") + 1);
        postingList2 = postingList2.substring(postingList2.indexOf(";") + 1);
        int lastDocID1 = PostingList.calculateLastDocID(postingList1);
        String firstDocID = postingList2.substring(0, postingList2.indexOf(" "));
        int firstDocID2 = Integer.parseInt(firstDocID) - lastDocID1;
        firstDocID = "" + firstDocID2;
        postingList2 = firstDocID + postingList2.substring(postingList2.indexOf(" "));
        return term + ";" + postingList1 + " " + postingList2;
    }

    /**
     * Returns the merged posting list from the two input posting lists. (for threads use)
     * @param postingList1- posting list to merge
     * @param postingList2- posting list to merge.
     * @return - the merged posting list from the two i.nput posting lists.
     */
    private String mergeAndSortPostingLists(String postingList1, String postingList2) {
        String term = extractTerm(postingList1);
        PostingList postingList_1 = new PostingList(postingList1);
        PostingList postingList_2 = new PostingList(postingList2);
        return term + ";" + PostingList.mergeLists(postingList_1, postingList_2);
    }

    /**
     * handle CapitalWord law. meaning checking collisions between similar words in dictionary.
     * @param aTerm- word to check.
     */
    private void handleCapitalWord(ATerm aTerm) {
        String term = aTerm.getTerm();
        String termLowerCase = term.toLowerCase();
        String termUpperCase = term.toUpperCase();
        if (term.equals(""))
            return;
        //term is upper case.
        if (Character.isUpperCase(term.charAt(0))) {
            if (dictionary.containsKey(termLowerCase)) {
                ((WordTerm) aTerm).toLowerCase();
            }
        }
        //term is lower case.
        else {
            if (dictionary.containsKey(termUpperCase)) {
                //change termUpperCase in dictionary to termLowerCase
                Pair<Integer, Integer> dictionaryPair = dictionary.remove(termUpperCase);
                dictionary.put(termLowerCase, dictionaryPair);
            }
        }
    }

    /**
     * Sorts and write dictionary to file dictionary in disk.
     */
    private void sortAndWriteDictionaryToDisk() {
        String fileName;
        if(useStemming)
            fileName="dictionaryStemming.txt";
        else
            fileName="dictionary.txt";
        String pathName = postingFilesPath + fileSeparator + fileName;
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            List<String> keys = new ArrayList<>(dictionary.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                Pair<Integer, Integer> dictionaryPair = dictionary.get(key);
                bw.write(key + ":" + dictionaryPair.getKey());
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds input term to city index
     * @param aTerm - city term to add to index.
     * @param docID - document docId of city term to add to index.
     */
    private void addToCityIndex(ATerm aTerm, int docID) {
        CityTerm cityTerm = (CityTerm) aTerm;
        List<CityPostingEntry> postingsList;
        List<Integer> positions = cityTerm.getPositions();
        CityPostingEntry postingEntry = new CityPostingEntry(docID, positions);
        String term = aTerm.getTerm();
        if (!cityInvertedIndex.containsKey(term)) {
            postingsList = new ArrayList<>();
            cityInvertedIndex.put(term, new Pair<>(cityTerm, postingsList));
        } else {
            postingsList = cityInvertedIndex.get(term).getValue();
        }
        postingsList.add(postingEntry);
    }

    /**
     * Resets vars for another use of the index in the current program run.
     */
    private void resetIndex() {
        blockNum.set(0);
        tempInvertedIndex.clear();
        usedMemory=0;
    }
    //</editor-fold>

    //<editor-fold desc="Setters">
    public void setIsStemming(boolean useStemming) {
        this.useStemming=useStemming;
    }

    public void setDictionary(Map<String, Pair<Integer, Integer>> dictionary) {
        this.dictionary = dictionary;
    }

    public void setPostingFilesPath(String postingFilesPath) {
        this.postingFilesPath = postingFilesPath;
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    public Map<String, Pair<Integer, Integer>> getDictionary() {
        return dictionary;
    }

    public int getDictionarySize() {
        if (dictionary == null)
            return 0;
        return dictionary.size();
    }
    //</editor-fold>
}
