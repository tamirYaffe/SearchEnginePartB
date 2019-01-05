package SearchEngineTools.Ranker;

import SearchEngineTools.Document;
import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.PostingEntry;
import SearchEngineTools.PostingList;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Ranker {
    private List <Document> rankedDocs;
    private Map<String, Pair<Integer, Integer>> dictionary;
    private int numOfDocumentsToReturn=50;
    private  String postingFilesPath;
    private  boolean useStemming;

    //queues for keeping only numOfDocumentsToReturn while ranking documents.
    private PriorityQueue<Document>maxRankedDocs=new PriorityQueue<>(new MaxRankedDocumentsComparator());
    private PriorityQueue<Document>minRankedDocs=new PriorityQueue<>(new MinRankedDocumentsComparator());


    public Ranker(Map<String, Pair<Integer, Integer>> dictionary, String postingFilesPath, boolean useStemming) {
        this.rankedDocs = new ArrayList<>();
        this.dictionary=dictionary;
        this.postingFilesPath=postingFilesPath;
        this.useStemming=useStemming;
    }

    public static void main(String[] args) {
        TreeSet<Document> test=new TreeSet<>(new MaxRankedDocumentsComparator());
        for (int i = 0; i < 100; i++) {
            Document document=new Document(i);
            if(test.size()<50)
                test.add(document);
            else
            if(test.last().getDocID()<document.getDocID()){
                test.add(document);
                test.pollLast();
            }

        }
        Iterator<Document> iterator=test.iterator();
        while(iterator.hasNext())
            System.out.println(iterator.next().getDocID());
    }

    public List<Document> rankDocuments(List<ATerm> queryTitleTerms, List<ATerm> queryDiscriptionTerms){
        rankedDocs.clear();
        maxRankedDocs.clear();
        minRankedDocs.clear();

        List<ATerm> queryTerms=new ArrayList<>();
        queryTerms.addAll(queryTitleTerms);
        if(queryDiscriptionTerms!=null)
            queryTerms.addAll(queryDiscriptionTerms);

        Document currDocument=null;
        List<PostingList> postingLists=getQueryPostingLists(queryTerms);
        System.out.println("all posting lists from disk loaded");//fixme:remove

        //a priority queue that holds posting entry and the termID of queryTerms.
        PriorityQueue<Pair<PostingEntry,Integer>> documentQueue=new PriorityQueue<>(Comparator.comparingInt(o -> o.getKey().getDocID()));
        for (int i = 0; i <postingLists.size() ; i++) {
            if(postingLists.get(i)!=null){
                PostingEntry postingEntry = postingLists.get(i).RemoveFirst();
                while(postingEntry!=null){
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
        double k=1.6;
        double b=0.75;
        double df=dictionary.get(term.getTerm()).getKey();
        int numOfDocs=Document.getNumOfDocs();
        double idf=Math.log((numOfDocs+1)/df);
        int documentLength=document.getDocLength();
        double avgDocLength=Document.getAvgDocLength();
        double rank=term.getOccurrences()*(k+1)*termTF/(termTF+k*(1-b+b*(documentLength/avgDocLength)))*idf;
        if(!isTitle)
            rank=rank*0.2;
        document.setDocRank(document.getDocRank()+rank);
    }

    private Pair<PostingEntry,Integer> getNextDocumentPair(PriorityQueue<Pair<PostingEntry, Integer>> documentQueue, List<PostingList> postingLists) {
        Pair <PostingEntry, Integer> pooled=documentQueue.poll();
        PostingEntry currPostingEntry=pooled.getKey();
        int postingListIndex=pooled.getValue();
        PostingEntry postingEntry= postingLists.get(postingListIndex).RemoveFirst();
        if(postingEntry!=null)
            documentQueue.add(new Pair<>(postingEntry,postingListIndex));
        return new Pair<>(currPostingEntry,postingListIndex);
    }

    private List<PostingList> getQueryPostingLists(List<ATerm> querryTerms) {
        List<PostingList> postingLists=new ArrayList<>();
        for (int i = 0; i < querryTerms.size(); i++) {
            postingLists.add(i,getTermPostingList(querryTerms.get(i)));
        }
        return postingLists;
    }

    private PostingList getTermPostingList(ATerm querryTerm) {
        if(dictionary.containsKey(querryTerm.getTerm())){
            int postingListPointer=dictionary.get(querryTerm.getTerm()).getValue();
            String postingListS= readPostingList(postingListPointer);
            return new PostingList(postingListS);
        }
        return null;
    }

    private String readPostingList(int postingListPointer) {
        String line=null ;
        String fileSeparator=System.getProperty("file.separator");
        String file_Name;
        if(useStemming)
            file_Name="postingListsStemming.txt";
        else
            file_Name="postingLists.txt";
        String pathName=postingFilesPath+fileSeparator+file_Name;
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (int i = 0; i < postingListPointer; i++)
                br.readLine();
            line=br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

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
}
