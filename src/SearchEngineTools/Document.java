package SearchEngineTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a document.
 *
 */
public class Document{
    //static vars
    public static String corpusPath;
    public static String postingFilesPath;
    private static boolean useStemming;

    private int docID;

    private String path;
    private int startLine;
    private int numOfLines;
    private int max_tf;
    private int numOfUniqeTerms;
    private  String docCity;

    //added
    private List<String> docLines;
    private double docRank;
    private int docLength;
    private static double avgDocLength;
    private static int numOfDocs;
    private String DOCNO;

    /**
     * A constructor for docId.
     * @param docID- the document line number in the Documents file.
     */
    public Document(int docID) {
        this.docID =docID;
        docLines=null;
    }


    /**
     * Returns the document lines.
     * @return- the document lines.
     */
    private List<String> getDocumentsLines() {
        loadDocPointerInfo();
        List<String> fileList = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            for (int i = 0; i < startLine; i++) {
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
     * Loads the document startLine, numOfLines and path from documents file to class vars.
     */
    private void loadDocPointerInfo() {
        String[] line ;
        String fileSeparator=System.getProperty("file.separator");
        String file_Name;
        if(useStemming)
            file_Name="DocumentsStemming.txt";
        else
            file_Name="Documents.txt";
        String pathName=postingFilesPath+fileSeparator+file_Name;
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (int i = 0; i < docID; i++)
                br.readLine();
            line=br.readLine().split(" ");
            String fileName=line[0];
            startLine= Math.toIntExact(Long.valueOf(line[1]));
            numOfLines= Math.toIntExact(Long.valueOf(line[2]));
            path=corpusPath+fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the document max_tf, numOfUniqeTerms and DOCNO from documents file to class vars.
     */
    public void loadDocInfo() {
        String[] line ;
        String fileSeparator=System.getProperty("file.separator");
        String file_Name;
        if(useStemming)
            file_Name="DocumentsInfoStemming.txt";
        else
            file_Name="DocumentsInfo.txt";
        String pathName=postingFilesPath+fileSeparator+file_Name;
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (int i = 0; i < docID; i++)
                br.readLine();
            line=br.readLine().split(" ");
            max_tf= Math.toIntExact(Long.valueOf(line[0]));
            numOfUniqeTerms= Math.toIntExact(Long.valueOf(line[1]));
            docLength =Math.toIntExact(Long.valueOf(line[2]));
            if(line.length>3)
                DOCNO=line[3];
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
            String toWrite=max_tf+" "+numOfUniqeTerms+" "+ docLength;
            if(DOCNO!=null)
                toWrite+=" "+DOCNO;
            if(docCity!=null)
                toWrite+=" "+docCity;
            out.println(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Document))
            return false;
        Document other= (Document) obj;
        return this.docID==other.docID;
    }

    //<editor-fold desc="Getters">
    public List<String> getDocLines() {
        if(docLines==null || docLines.isEmpty()){
            docLines=getDocumentsLines();
        }
        return docLines;
    }

    public String getDOCNO() {
        return DOCNO;
    }

    public int getNumOfUniqeTerms() {
        return numOfUniqeTerms;
    }

    public int getDocID() {
        return docID;
    }

    public double getDocRank() {
        return docRank;
    }

    public static double getAvgDocLength() {
        if(avgDocLength==0)
            loadDocumentsInfoLength();
        return avgDocLength;
    }

    public static int getNumOfDocs() {
        if(numOfDocs==0)
            loadDocumentsInfoLength();
        return numOfDocs;
    }

    public int getDocLength() {
        return docLength;
    }
    //</editor-fold>

    //<editor-fold desc="setters">
    public void setDocCity(String docCity) {
        this.docCity = docCity;
    }

    public void setDOCNO(String DOCNO) {
        this.DOCNO = DOCNO;
    }

    public void setDocRank(double docRank) {
        this.docRank = docRank;
    }

    public static void setDocumentsInfo(double avgDocLength,int numOfDocs) {
        Document.avgDocLength = avgDocLength;
        Document.numOfDocs=numOfDocs;
        writeAvgDocumentsInfoToDisk(avgDocLength,numOfDocs);
    }


    public static void setUseStemming(boolean useStemming) {
        Document.useStemming = useStemming;
    }

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }
//</editor-fold>

    private static void writeAvgDocumentsInfoToDisk(double avgDocLength, int numOfDocs) {
        String fileSeparator=System.getProperty("file.separator");
        String pathName = postingFilesPath + fileSeparator +"DocumentsAdditionalInfo.txt";
        File file = new File(pathName);
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(""+avgDocLength+","+numOfDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDocumentsInfoLength() {
        String fileSeparator=System.getProperty("file.separator");
        String pathName=postingFilesPath+fileSeparator+"DocumentsAdditionalInfo.txt";
        File file = new File(pathName);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line=br.readLine();
            avgDocLength =Double.parseDouble(line.split(",")[0]);
            numOfDocs =Integer.parseInt(line.split(",")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
