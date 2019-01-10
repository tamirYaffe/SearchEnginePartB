package SearchEngineTools.Ranker;

import SearchEngineTools.Document;
import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;
import SearchEngineTools.PostingEntry;
import SearchEngineTools.PostingList;
import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * A class that represents rank process.
 */
public class Ranker {
    //List of ranked documents
    /**
     * List of ranked Documents
     */
    private List <Document> rankedDocs;

    //
    /**
     * The Corpus's dictionary (stemmed corresponding to the stemm field)
     */
    private Map<String, Pair<Integer, Integer>> dictionary;

    /**
     * Maximal amount of documents returned for a query
     */
    private int numOfDocumentsToReturn=50;
    /**
     * path to posting path
     */
    private  String postingFilesPath;
    /**
     * whether or not use stemming
     */
    private  boolean useStemming;

    //queues for keeping only numOfDocumentsToReturn while ranking documents.
    private PriorityQueue<Document>maxRankedDocs=new PriorityQueue<>(new MaxRankedDocumentsComparator());
    private PriorityQueue<Document>minRankedDocs=new PriorityQueue<>(new MinRankedDocumentsComparator());

    /**
     * Constructor
     * @param dictionary- relevant corpus dictionary.
     * @param postingFilesPath- the posting lists path.
     * @param useStemming- boolean for using stemming.
     */
    public Ranker(Map<String, Pair<Integer, Integer>> dictionary, String postingFilesPath, boolean useStemming) {
        this.rankedDocs = new ArrayList<>();
        this.dictionary=dictionary;
        this.postingFilesPath=postingFilesPath;
        this.useStemming=useStemming;
    }

    /**
     * Rank's the corpus documents by the input queryTitleTerms and queryDescriptionTerms.
     * @param queryTitleTerms - the query title terms.
     * @param queryDescriptionTerms - the query description terms.
     * @param allowedDocuments
     * @return List of top ranked documents, size of numOfDocumentsToReturn.
     */
    public List<Document> rankDocuments(List<ATerm> queryTitleTerms, List<ATerm> queryDescriptionTerms, List<Document> allowedDocuments){
        rankedDocs.clear();
        maxRankedDocs.clear();
        minRankedDocs.clear();

        List<ATerm> queryTerms=new ArrayList<>();
        queryTerms.addAll(queryTitleTerms);
        if(queryDescriptionTerms!=null)
            queryTerms.addAll(queryDescriptionTerms);

        Document currDocument=null;
        queryTerms=modifyQueryTerms(queryTerms);
//        List<PostingList> postingLists=getAllQueryPostingLists(queryTerms);
        List<PostingList> postingLists=getAllQueryPostingLists(queryTerms);
        System.out.println("all posting lists from disk loaded");//fixme:remove

        //a priority queue that holds posting entry and the termID of queryTerms.
        PriorityQueue<Pair<PostingEntry,Integer>> documentQueue=new PriorityQueue<>(Comparator.comparingInt(o -> o.getKey().getDocID()));
        for (int i = 0; i <postingLists.size() ; i++) {
            if(postingLists.get(i)!=null){
                PostingEntry postingEntry = postingLists.get(i).RemoveFirst();
                while(postingEntry!=null){
                    if(allowedDocuments==null || allowedDocuments.contains(new Document(postingEntry.getDocID())))
                        documentQueue.add(new Pair<>(postingEntry,i));
                    postingEntry= postingLists.get(i).RemoveFirst();
                }
            }
        }

        HashMap<Integer,Pair<Integer,String>> documentsInfo=getDocumentsInfo(new PriorityQueue<>(documentQueue));

        //for each document
        while(!documentQueue.isEmpty()){
            //get next posting entry and termID
            Pair<PostingEntry,Integer> docTermPair=documentQueue.poll();
            PostingEntry currPostingEntry=docTermPair.getKey();
            Document nextDocument=new Document(currPostingEntry.getDocID());
            if(!nextDocument.equals(currDocument)){
                //addTorRankedDocs
                if(currDocument!=null){
                    addToRankedDocs(currDocument);
                }
                currDocument=nextDocument;
                Pair<Integer,String> documentInfo=documentsInfo.get(currDocument.getDocID());
                currDocument.setDocLength(documentInfo.getKey());
                currDocument.setDOCNO(documentInfo.getValue());
            }
            //rank document
            if(queryTitleTerms.contains(docTermPair.getValue()))
                rankDocument(currDocument,queryTerms.get(docTermPair.getValue()),currPostingEntry.getTermTF(),true);
            else
                rankDocument(currDocument,queryTerms.get(docTermPair.getValue()),currPostingEntry.getTermTF(),false);
        }
        //adding last document
        addToRankedDocs(currDocument);
        System.out.println("adding to final list");//fixme:remove
        addTopRankedDocumentsToFinalList();
        return rankedDocs;
    }

    /**
     * Add's top ranked documents to rankedDocuments.
     */
    private void addTopRankedDocumentsToFinalList() {
        while (!maxRankedDocs.isEmpty())
            rankedDocs.add(maxRankedDocs.poll());
    }

    /**
     * Rank's the input document by BM25 function.
     * @param document
     * @param termTF
     */
    private void rankDocument(Document document, ATerm term, int termTF, boolean isTitle) {
        double k=1.8;
        double b=0.75;
        double df=dictionary.get(term.getTerm()).getKey();
        int numOfDocs=Document.getNumOfDocs();
        double idf=Math.log((numOfDocs+1)/df);
        int documentLength=document.getDocLength();
        double avgDocLength=Document.getAvgDocLength();
        double rank=term.getOccurrences()*(k+1)*termTF/(termTF+k*(1-b+b*(documentLength/avgDocLength)))*idf;
        if(!isTitle)
            rank=rank*0.05;
        document.setDocRank(document.getDocRank()+rank);
    }

    /**
     * Get's and Return's all the posting lists from the of the input query terms.
     * @param queryTerms- the query terms.
     * @return- List of the posting lists.
     */
    private List<PostingList> getAllQueryPostingLists(List<ATerm> queryTerms) {
        List<PostingList> postingLists;
        PriorityQueue<Pair<Integer,Integer>>postingListsIndex=new PriorityQueue<>(Comparator.comparingInt(Pair::getValue));
        for (int i = 0; i <queryTerms.size() ; i++) {
            postingListsIndex.add(new Pair<>(i,dictionary.get(queryTerms.get(i).getTerm()).getValue()));
        }
        postingLists=getPostingLists(postingListsIndex,queryTerms.size());
        return postingLists;
    }

    /**
     * Add's the input document to ranked document priorities queues.
     * @param document- the document to add.
     */
    private void addToRankedDocs(Document document){
        if(document==null)
            return;
        if(maxRankedDocs.size()<numOfDocumentsToReturn){
            maxRankedDocs.add(document);
            minRankedDocs.add(document);
        }
        else
            if(minRankedDocs.peek().getDocRank()<document.getDocRank()){
                maxRankedDocs.add(document);
                minRankedDocs.add(document);
                //removing the min rank document from both queues.
                maxRankedDocs.remove(minRankedDocs.poll());
            }
    }

    /**
     * Loads documents info to returned hashMap for all documents in input documentQueue in one file read linear pass.
     * @param documentQueue- documents we wish to get info about.
     * @return- hashMap with documents info.
     */
    private HashMap<Integer, Pair<Integer, String>> getDocumentsInfo(PriorityQueue<Pair<PostingEntry, Integer>> documentQueue) {
        HashMap<Integer, Pair<Integer, String>> documentsInfo=new HashMap<>();
        String fileSeparator=System.getProperty("file.separator");
        String file_Name;
        if(useStemming)
            file_Name="DocumentsInfoStemming.txt";
        else
            file_Name="DocumentsInfo.txt";
        String pathName=postingFilesPath+fileSeparator+file_Name;
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int currLineNum=0;
            line=br.readLine();
            while(!documentQueue.isEmpty()){
                int currDocID=documentQueue.poll().getKey().getDocID();
                while(currLineNum<currDocID){
                    line=br.readLine();
                    currLineNum++;
                }
                int docLength =Math.toIntExact(Long.valueOf(line.split(" ")[2]));
                String DOCNO=line.split(" ")[3];
                Pair<Integer,String> currDocumentInfo=new Pair<>(docLength,DOCNO);
                documentsInfo.put(currDocID,currDocumentInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentsInfo;
    }

    /**
     * Get's and Return's all the posting lists from the of the input postingListsIndex.
     * @param postingListsIndex- queue of pairs of termID and postingList index.
     * @param size-size of the returned list.
     * @return
     */
    private List<PostingList> getPostingLists(PriorityQueue<Pair<Integer, Integer>> postingListsIndex, int size) {
        PostingList[]postingListsArray=new PostingList[size];
        String fileSeparator=System.getProperty("file.separator");
        String file_Name;
        if(useStemming)
            file_Name="postingListsStemming.txt";
        else
            file_Name="postingLists.txt";
        String pathName=postingFilesPath+fileSeparator+file_Name;
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int currLineNum=0;
            line=br.readLine();
            while(!postingListsIndex.isEmpty()){
                Pair<Integer,Integer>pairPolled=postingListsIndex.poll();
                int termID=pairPolled.getKey();
                int postingIndex=pairPolled.getValue();
                while(currLineNum<postingIndex){
                    line=br.readLine();
                    currLineNum++;
                }
                postingListsArray[termID]=new PostingList(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(postingListsArray);
    }

    /**
     * Check and return modify list of only the terms of queryTerms which appear in the corpus dictionary.
     * @param queryTerms- list of query terms.
     * @return-  modify list of only the terms of queryTerms which appear in the corpus dictionary.
     */
    private List<ATerm> modifyQueryTerms(List<ATerm> queryTerms) {
        List<ATerm>modifyQueryTerms=new ArrayList<>();
        for (int i = 0; i < queryTerms.size(); i++) {
            ATerm aTerm=queryTerms.get(i);
            String term=aTerm.getTerm();
            if(dictionary.containsKey(term))
                modifyQueryTerms.add(aTerm);
            else{
                if(aTerm instanceof WordTerm && dictionary.containsKey(term.toLowerCase())){
                    ((WordTerm) aTerm).toLowerCase();
                    modifyQueryTerms.add(aTerm);
                }
            }
        }
        return modifyQueryTerms;
    }
}
