package Model;

import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.Ranker.Ranker;
import SearchEngineTools.ReadFile;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;

/**
 * Logic behind the search engine
 */
public class Model extends Observable {
    /**
     * readfile using to read files
     */
    private ReadFile readFile;
    /**
     * indexer used to index
     */
    private Indexer indexer;
    /**
     * used to filter by cities
     */
    private CityFilter cityFilter;


    /**
     * initializes indexer
     */
    public Model() {
        indexer = new Indexer(1048576 * 10);
    }

    /**
     * index a corpus
     * @param corpusPath path to corpus
     * @param postingFilesPath path to postinglist
     * @param useStemming true->use stemming,false->do not use stemming
     * @return number of docs, number of unique terms in corpus, runtime
     */
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

    /**
     * load new dictionary
     * @param dictionary dictionary to load
     */
    public void loadDictionary(Map<String, Pair<Integer, Integer>> dictionary) {
        indexer.setDictionary(dictionary);
    }

    /**
     * Deletes all files in specified Docs. Removes current Dictionary and city filter
     * @param postingFilePath
     */
    public void deleteAll(String postingFilePath) {
        indexer.clear();
        ReadFile.deletePostingFiles(postingFilePath);
        cityFilter = null;
    }

    /**
     * Gets All Languages of the documents
     * @return All Languages of the documents
     */
    public Collection<String> getLanguages() {
        return readFile.getLanguages();
    }

    /**
     * Evaluates a query and returns list of relevant documents ordered by relevance
     * @param query query to evaluate
     * @param postingListPath path to postinglist
     * @param useStemming whether to use stemming
     * @param useSemantics wether to use semantics
     * @param resultLocationPath path of result location
     * @param corpusPath path of corpus
     * @return list of relevant documents ordered by relevance
     */
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

    /**
     * Runs queries on file of the specified format. Writes result to file called results.txt in sepecified folder
     * @param queryFilePath path to queryfile
     * @param postingListPath path to postingList
     * @param spellCheck whether to automatically spellcheck
     * @param useStemming whether to use stemming
     * @param useSemantics whether to handle semantics
     * @param resultLocationPath location of results file
     * @param corpusPath path of corpus
     */
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

    /**
     * add city to selected
     * @param cityName Name of city
     */
    public void addCity(String cityName){
        cityFilter.addToSelected(cityName);
    }

    /**
     * removes city from selected
     * @param cityName name of city
     */
    public void removeCity(String cityName){
        cityFilter.removeFromSelected(cityName);
    }

    /**
     * Gets all city names
     * @param postingFilePath path of postinglist
     * @param cityFilePath path of city index
     * @return List of all city names
     * @throws IOException if cannot find one of the files
     */
    public List<String> getAllCityNames(String postingFilePath,String cityFilePath) throws IOException {
        if(cityFilter==null)
            cityFilter = new CityFilter(indexer.getDictionary(),postingFilePath,cityFilePath);
        return cityFilter.getAllCityNames();
    }

    /**
     * All Selected CityNames
     * @return All Selected CityNames
     */
    public Collection<String> getAllSelectedCityNames(){
        return cityFilter.getSelectedCities();
    }

    /**
     * select all cities or remove all of them
     * @param selectAll true->select all cities, false->remove all cities
     */
    public void selecAllCities(boolean selectAll){
        cityFilter.selectOrRemoveAll(selectAll);
    }

    /**
     * true if all cities are selected
     * @return true if all cities are selected, false otherWise
     */
    public boolean allCitiesSelected(){
        return cityFilter.allSelected();
    }

    /**
     * set the cityfilter for this model
     * @param cityFilter
     */
    public void setCityFilter(CityFilter cityFilter){
        this.cityFilter = cityFilter;
    }

    /**
     * gets size of dictionary
     * @return 0 if null, otherwise returns dictionary size
     */
    public int getDictionarySize(){
        if(indexer.getDictionary()==null)
            return 0;
        return indexer.getDictionarySize();
    }

    /**
     * Gets stopwords from corpus
     * @param corpusPath path of corpus
     * @return corpus's stopwords
     */
    public Collection<String> getStopWords(String corpusPath){
        return ReadFile.getStopWords(corpusPath);
    }

}
