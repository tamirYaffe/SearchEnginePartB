package SearchEngineTools;

import SearchEngineTools.ParsingTools.Parse;
import SearchEngineTools.ParsingTools.ParseWithStemming;
import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.Ranker.Ranker;
import javafx.util.Pair;
import sun.awt.Mutex;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A class used for reading the corpus and starting parse and index process.
 */
public class ReadFile {
    private int numOfDocs;
    private Parse parse;
    private Indexer indexer;
    private HashSet<String> stopWords = new HashSet<>();
    private String corpusPath;
    private String postingFilesPath;
    private String fileSeparator = System.getProperty("file.separator");
    private boolean useStemming;
    //added
    private int documentsSumLenght;
    private Ranker ranker;

    //for documents
    private List<String> documentsBuffer = new ArrayList<>();
    private int documentBufferSize;

    //threads use
    private ConcurrentBuffer<Pair<Iterator<ATerm>, Document>> PIBuffer = new ConcurrentBuffer<>(Integer.MAX_VALUE);
    private Mutex mutex = new Mutex();

    /**
     * Constructor
     * @param indexer- indexer to use.
     * @param corpusPath- the corpus path.
     * @param postingFilesPath- the posting files path.
     * @param useStemming- indicates which parse to use..
     */
    public ReadFile(Indexer indexer, String corpusPath, String postingFilesPath, boolean useStemming) {
        this.corpusPath = corpusPath;
        this.postingFilesPath = postingFilesPath;
        //added
        Document.postingFilesPath=postingFilesPath;
        this.indexer = indexer;
        if (useStemming)
            parse = new ParseWithStemming();
        else
            parse = new Parse();
        this.useStemming=useStemming;
        File directory = new File("blocks");
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    public void setRanker(Ranker ranker) {
        this.ranker = ranker;
    }

    /**
     * The main method of the class, controls all other methods.
     * @return- number of docs in the corpus.
     */
    public int listAllFiles() {
        numOfDocs=0;
        String path = corpusPath;
        createStopWords(path);
        Document.corpusPath = path;
        startIndexThread();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        if (!filePath.toString().contains("stop_words")) {
                            divideFileToDocs(readContent(filePath), filePath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeDocumentsToDisk();
        System.out.println("stoping indexer");
        PIBuffer.add(new Pair<>(null, new Document(-1)));
        //write remaining posting lists to disk
        mutex.lock();
        try {
            indexer.sortAndWriteInvertedIndexToDisk();
            indexer.mergeBlocks();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        deletePrevFiles();
        Document.setDocumentsInfo(documentsSumLenght/numOfDocs,numOfDocs);
        return numOfDocs;
    }

    /**
     * Load stop_words.txt to stopWords var.
     * @param path- the path of the stop words file.
     */
    private void createStopWords(String path) {
        File root = new File(path);
        String fileName = "stop_words.txt";
        try {
            boolean recursive = true;
            Collection files = FileUtils.listFiles(root, null, recursive);
            for (Iterator iterator = files.iterator(); iterator.hasNext(); ) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName))
                    readStopWords(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        parse.setStopWords(stopWords);
    }

    /**
     * Reads the content of the file.
     * @param filePath - path of the file.
     * @return - list of file lines.
     */
    private List<String> readContent(Path filePath) {

        BufferedReader br = null;
        FileReader fr = null;
        List<String> fileList = new ArrayList<>();
        String line;
        try {
            fr = new FileReader(filePath.toString());
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                fileList.add(line);
            }
        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
        return fileList;
    }

    /**
     * Divide the input fileList into documents and activate the parse on them.
     * @param fileList- the file lines to divide.
     * @param filePath- the path of the file.
     */
    private void divideFileToDocs(List<String> fileList, Path filePath) {
        List<String> docLines = new ArrayList<>();
        String docName;
        int startLineNumInt = 0;
        int endLineNumInt = 0;
        int numOfLinesInt = 0;
        String docNO=null;
        for (String line : fileList) {
            docLines.add(line);
            if(line.contains("<DOCNO>")){
                docNO=line.substring(7,line.length()-8);
                if(docNO.charAt(0)==' ')
                    docNO=docNO.substring(1);
                if(docNO.charAt(docNO.length()-1)==' ')
                    docNO=docNO.substring(0,docNO.length()-1);
            }
            endLineNumInt++;
            numOfLinesInt++;
            if (line.equals("</DOC>")) {
                Collection<ATerm> terms = parse.parseDocument(docLines);
                int docLength=parse.getLastParsedDocumentLength();
                documentsSumLenght+=docLength;
                createDoc(filePath, startLineNumInt, numOfLinesInt);
                //add the parse terms to the producer-consumer buffer.
                Document document=new Document(numOfDocs);
                document.setDocLength(docLength);
                document.setDOCNO(docNO);
                PIBuffer.add(new Pair(terms.iterator(), document));
                startLineNumInt = endLineNumInt + 1;
                numOfLinesInt = 0;
                docLines.clear();
                numOfDocs++;
            }
        }

    }

    /**
     * Starting the index thread.
     * the thread is the consumer of the Concurrent Buffer.
     */
    private void startIndexThread() {
        System.out.println("starting indexing");
        Thread createIndex = new Thread(() -> {
            mutex.lock();
            while (true) {
                Pair<Iterator<ATerm>, Document> toIndex = PIBuffer.get();
                if (toIndex.getValue().getDocID() == -1) {
                    break;
                }
                indexer.createInvertedIndex(toIndex.getKey(), toIndex.getValue());
            }
            mutex.unlock();
        });
        createIndex.start();
//        threadPool.execute(createIndex);
    }

    /**
     * Adds the input vars to document buffer that writes them to Documents file in disk.
     * @param filePath- the file path.
     * @param startLineNum- the line that the document start in.
     * @param numOfLines- number of the document lines.
     */
    private void createDoc(Path filePath, int startLineNum, int numOfLines) {
        String fileName = extractFileName(filePath.toString());
        String documentLine = fileName + " " + startLineNum + " " + numOfLines;
        documentBufferSize += documentLine.length() + 1;
        documentsBuffer.add(documentLine);
        if (documentBufferSize > 1048576 * 5) {
            writeDocumentsToDisk();
            documentsBuffer.clear();
            documentBufferSize = 0;
        }
    }

    /**
     * Writing the documents index to file Documents in disk.
     */
    private void writeDocumentsToDisk() {
        String fileName;
        if(useStemming)
            fileName="DocumentsStemming.txt";
        else
            fileName="Documents.txt";
        String pathName = postingFilesPath + fileSeparator +fileName;
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < documentsBuffer.size(); i++) {
                bw.write(documentsBuffer.get(i));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract and returns the input path file name.
     * @param path- the file path.
     * @return -the input path file name.
     */
    private String extractFileName(String path) {
        String[] splitPath;
        String fileName;
        splitPath = path.split(Pattern.quote(fileSeparator));
        fileName = fileSeparator + splitPath[splitPath.length - 1] + fileSeparator + splitPath[splitPath.length - 2];
        return fileName;
    }

    /**
     * Deleting previous blocks.
     */
    public static void deletePrevFiles() {
        File dir = new File("blocks");
        for (File file : dir.listFiles()){
            if (!file.isDirectory())
                file.delete();
        }
        dir.delete();
    }

    /**
     * Reads the stop words into the stopWords var.
     * @param filePath- the path of the stop words.
     */
    private void readStopWords(File filePath) {
        BufferedReader br = null;
        FileReader fr = null;
        String line;
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                stopWords.add(line);
            }
        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }

    /**
     * Clears all the class data structures.
     */
    public void clear() {
        stopWords.clear();
    }

    /**
     * Returns all the corpus documents languages.
     * @return- all the corpus documents languages.
     */
    public Collection <String> getLanguages(){
        return parse.getAllDocumentLanguages();
    }

    public List<Document> runQueryFromUser(String query, boolean spellCheck, int numOfSynonyms){
        deleteResultsFile();
        List<String> queryLine=new ArrayList<>();
        queryLine.add(query);
        int queryID= (int) (Math.random()*100);
        return runQuery(queryLine,null,spellCheck,numOfSynonyms,queryID);
    }

    public void runQueriesFromFile(String queriesFilePath,boolean spellCheck, int numOfSynonyms){
        deleteResultsFile();
        List<String>queryTitleLines=new ArrayList<>();
        List<String>queryDescLines=new ArrayList<>();
        List<String>fileContent=readContent(Paths.get(queriesFilePath));
        int queryID=-1;
        for (int i=0;i<fileContent.size();i++) {
            String line=fileContent.get(i);
            if(line.contains("<num>")){
                String queryIDString=line.substring(line.indexOf(":")+2);
                if(queryIDString.charAt(queryIDString.length()-1)==' ')
                    queryIDString=queryIDString.substring(0,queryIDString.length()-1);
                queryID= Integer.parseInt(queryIDString);
            }
            if(line.contains("<title>"))
                queryTitleLines .add(line.substring(7));
            if(line.contains("<desc>")){
                i++;
                line=fileContent.get(i);
                while(!line.contains("<narr>")){
                    queryDescLines.add(line);
                    i++;
                    line=fileContent.get(i);
                }
            }
            if (line.equals("</top>")) {
                System.out.println("running query: "+queryID);
                runQuery(queryTitleLines,queryDescLines,spellCheck,numOfSynonyms,queryID);
                System.out.println("finished query: "+queryID);
                queryTitleLines.clear();
                queryDescLines.clear();
            }
        }
    }

    private List<Document> runQuery(List<String> queryTitle, List<String> queryDiscription, boolean spellCheck, int numOfSynonyms, int queryID){
        List<ATerm> queryTitleTerms= parse.parseQuery(queryTitle,spellCheck,numOfSynonyms);
        List<ATerm> queryDiscriptionTerms=null;
        if(queryDiscription!=null)
            queryDiscriptionTerms=parse.parseQuery(queryDiscription,false,0);
        System.out.println("starting ranking query");//fixme:remove
        List<Document>releventDocuments=ranker.rankDocuments(queryTitleTerms,queryDiscriptionTerms);
        //save results to resFile.
        for(Document document:releventDocuments)
            writeResultToFile(queryID,0,document.getDOCNO(),document.getDocRank(),0,"tmr");
        return releventDocuments;
    }

    private void writeResultToFile(int queryID,int iter,String docNO,double rank,float sim,String run_ID) {
        String pathName = postingFilesPath + fileSeparator +"results.txt";
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(queryID+" "+iter+" "+docNO+" "+rank+" "+sim+" "+run_ID);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteResultsFile() {
        String pathName = postingFilesPath + fileSeparator +"results.txt";
        File file = new File(pathName);
        file.delete();
    }
}