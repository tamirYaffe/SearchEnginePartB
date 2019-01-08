package Model;

import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.Ranker.Ranker;
import SearchEngineTools.ReadFile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

public class Model extends Observable {
    private ReadFile readFile;
    private Indexer indexer;
    private CityFilter cityFilter;


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

    public void deleteAll(String postingFilePath) {
        indexer.clear();
        ReadFile.deletePostingFiles(postingFilePath);
    }

    public Collection<String> getLanguages() {
        return readFile.getLanguages();
    }

    public List<Document> queryNaturalLanguage(String query, String postingListPath, boolean useStemming, boolean useSemantics, String resultLocationPath,String corpusPath){
        readFile = new ReadFile(indexer, corpusPath,postingListPath,useStemming);
        int numOfSynonyms = useSemantics?1:0;
        Ranker ranker = new Ranker(indexer.getDictionary(),postingListPath,useStemming);
        readFile.setRanker(ranker);
        if(cityFilter==null || cityFilter.allSelected()){
            List<Document> toReturn = readFile.runQueryFromUser(query,false,numOfSynonyms,resultLocationPath,null);
            return toReturn;
        }
        else {
            List<Document> allowed = getAllowedDocuments();
            List<Document> toReturn = readFile.runQueryFromUser(query,false,numOfSynonyms,resultLocationPath,allowed);
            return toReturn;
        }
    }

    private List<Document> getAllowedDocuments(){
        Collection<Integer> allowedDocIDs = cityFilter.getSelectedDocIDs();
        List<Document> allowed = new ArrayList<>(allowedDocIDs.size());
        for (int i :allowedDocIDs) {
            allowed.add(new Document(i));
        }
        return allowed;
    }

    public void queryFromFile(String queryFilePath, String postingListPath,boolean spellCheck, boolean useStemming, boolean useSemantics, String resultLocationPath, String corpusPath){
        readFile = new ReadFile(indexer,corpusPath,postingListPath,useStemming);
        int numOfSynonyms = useSemantics?1:0;
        Ranker ranker = new Ranker(indexer.getDictionary(),postingListPath,useStemming);
        readFile.setRanker(ranker);
        if(cityFilter==null || cityFilter.allSelected()){
            readFile.runQueriesFromFile(queryFilePath,spellCheck,numOfSynonyms,resultLocationPath,null);
        }
        else {

            List<Document> allowed = getAllowedDocuments();
            readFile.runQueriesFromFile(queryFilePath,spellCheck,numOfSynonyms,resultLocationPath,allowed);
        }
    }

    public void addCity(String cityName){
        cityFilter.addToSelected(cityName);
    }

    public void removeCity(String cityName){
        cityFilter.removeFromSelected(cityName);
    }

    public List<String> getAllCityNames(String postingFilePath,String cityFilePath) throws IOException {
        if(cityFilter==null)
            cityFilter = new CityFilter(indexer.getDictionary(),postingFilePath,cityFilePath);
        return cityFilter.getAllCityNames();
    }

    public Collection<String> getAllSelectedCityNames(){
        return cityFilter.getSelectedCities();
    }

    public void selecAllCities(boolean selectAll){
        cityFilter.selectOrRemoveAll(selectAll);
    }

    public boolean allCitiesSelected(){
        return cityFilter.allSelected();
    }

    private void initializeReadFile(String corpusPath,String postingFilesPath, boolean useStemming){
            readFile = new ReadFile(indexer, corpusPath, postingFilesPath, useStemming);
    }

    public void setCityFilter(CityFilter cityFilter){
        this.cityFilter = cityFilter;
    }

    public int getDictionarySize(){
        if(indexer.getDictionary()==null)
            return 0;
        return indexer.getDictionarySize();
    }

}
