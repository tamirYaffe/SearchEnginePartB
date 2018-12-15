package SearchEngineTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a document.
 *
 */
public class Document {
    //static vars
    public static String corpusPath;
    private static boolean useStemming;

    private int docID;
    private String path;
    private Long startLine;
    private Long numOfLines;
    private int max_tf;
    private int numOfUniqeTerms;
    private  String docCity;

    /**
     * A constructor for docId.
     * @param docID- the document line number in the Documents file.
     */
    public Document(int docID) {
        this.docID =docID;
    }

    /**
     * Returns the document lines.
     * @return- the document lines.
     */
    public List<String> getDocumentsLines() {
        loadDocPointerInfo();
        List<String> fileList = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            for (int i = 1; i < startLine; i++) {
                reader.readLine();
            }
            for (int i = 0; i < numOfLines; i++) {
                fileList.add(reader.readLine());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * Loads the document startLine, numOfLines and path from documentsInfo file to class vars.
     */
    private void loadDocPointerInfo() {
        String[] line ;
        try (BufferedReader br = new BufferedReader(new FileReader("Documents.txt"))) {
            for (int i = 0; i < docID -1; i++)
                br.readLine();
            line=br.readLine().split(" ");
            String fileName=line[0];
            startLine= Long.valueOf(line[1]);
            numOfLines= Long.valueOf(line[2]);
            path=corpusPath+fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the documents max_tf and numOfUniqeTerms vars.
     * @param termOccurrences- the current term number of occurrences in the document.
     */
    public void updateDocInfo(int termOccurrences) {
        if(termOccurrences>max_tf)
            max_tf=termOccurrences;
        numOfUniqeTerms++;
    }

    /**
     * Writes the document info(max_tf, numOfUniqeTerms) and docCity if exists to DocumentsInfo file.
     * @param postingFilesPath- path of posting files.
     */
    public void writeDocInfoToDisk(String postingFilesPath){
        String fileSeparator=System.getProperty("file.separator");
        String fileName;
        if(useStemming)
            fileName="DocumentsInfoStemming.txt";
        else
            fileName="DocumentsInfo.txt";
        String pathName=postingFilesPath+fileSeparator+fileName;
        File file = new File(pathName);
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            String toWrite=max_tf+" "+numOfUniqeTerms;
            if(docCity!=null)
                toWrite+=" "+docCity;
            out.println(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //<editor-fold desc="setters">
    public void setDocCity(String docCity) {
        this.docCity = docCity;
    }

    public static void setUseStemming(boolean useStemming) {
        Document.useStemming = useStemming;
    }
    //</editor-fold>
}
