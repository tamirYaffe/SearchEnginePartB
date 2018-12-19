package Test;


import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.ParsingTools.Term.ATerm;
import SearchEngineTools.ParsingTools.Term.WordTerm;
import SearchEngineTools.ParsingTools.Token;
import SearchEngineTools.Ranker.Ranker;
import SearchEngineTools.ReadFile;

import java.io.File;
import java.util.*;

public class MergeBlocksTest {
    public static void main(String[] args) {
        int numOfDocs;
        boolean useStemming=false;
        String postingListsPath="/Users/tamiryaffe/Desktop/postingFiles";
        Indexer indexer = new Indexer(1048576 * 10);
        ReadFile readFile = new ReadFile(indexer, "/Users/tamiryaffe/Desktop/corpus", postingListsPath, useStemming);
        String path=postingListsPath;
        if(path.length()==0)
            return;
        File dir = new File(path);
        if(!useStemming) {
            for (File file : dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        }
        else {
            for (File file : dir.listFiles())
                if (!file.isDirectory() && file.getName().contains("Stemming"))
                    file.delete();
        }

        Document.setUseStemming(useStemming);
        Document.postingFilesPath=postingListsPath;
        indexer.setPostingFilesPath(postingListsPath);
        indexer.setIsStemming(useStemming);
        numOfDocs = readFile.listAllFiles();
        indexer.writeCityIndex();

        //partB test
        Ranker ranker=new Ranker(indexer.getDictionary(),postingListsPath,useStemming,numOfDocs);
        List<ATerm> queryTerms=new ArrayList<>();
        WordTerm wordTerm;

        wordTerm=new WordTerm(new Token("chinese",0));
        wordTerm.setOccurrences(1);
        queryTerms.add(wordTerm);

        wordTerm=new WordTerm(new Token("economically",0));
        wordTerm.setOccurrences(1);
        queryTerms.add(wordTerm);

        wordTerm=new WordTerm(new Token("achievements",0));
        wordTerm.setOccurrences(1);
        queryTerms.add(wordTerm);


        List<Document> rankedDocs=ranker.rankDocuments(queryTerms);
        Iterator<Document> iterator=rankedDocs.iterator();
        while(iterator.hasNext()){
            Document document=iterator.next();
            System.out.println(document.getDOCNO()+": "+document.getDocRank());
        }
    }
}
