package Model;

import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.ReadFile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;

public class Model extends Observable {
    private ReadFile readFile;
    private Indexer indexer;


    public Model() {
        indexer = new Indexer(1048576 * 10);
    }

    public String startIndex(String corpusPath, String postingFilesPath, boolean useStemming) {
        int numOfDocs;
        int numOfTerms;
        int lastRunTime;
        Document.setUseStemming(useStemming);

        //setting the indexer
        indexer.clear();
        indexer.setPostingFilesPath(postingFilesPath);
        indexer.setIsStemming(useStemming);

        readFile = new ReadFile(indexer, corpusPath, postingFilesPath, useStemming);
        long startTime = System.nanoTime();
        numOfDocs = readFile.listAllFiles();
        indexer.writeCityIndex();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        lastRunTime = (int) (duration / 1000000000);
        numOfTerms = indexer.getDictionarySize();
        setChanged();
        notifyObservers();
        return "number of documents indexed: " + numOfDocs+System.getProperty("line.separator")
                +"number of unique terms in corpus: " + numOfTerms+System.getProperty("line.separator")
                +"index runtime in seconds: " + lastRunTime;
    }

    public void loadDictionary(Map<String, Pair<Integer, Integer>> dictionary) {
        indexer.setDictionary(dictionary);
    }

    public void deleteAll() {
        indexer.clear();
    }

    public Collection<String> getLanguages() {
        return readFile.getLanguages();
    }
}
