package Model;

import SearchEngineTools.Document;
import SearchEngineTools.PostingEntry;
import SearchEngineTools.PostingList;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Used To Filter By Cities. Maintains list of selected cities and allows retrieval of selected Documents
 */
public class CityFilter {


    /**
     * Cities that are currently selected
     */
    private Collection<String> selectedCities=new HashSet<>();
    /**
     * Cities and their relevant Document IDs
     */
    private Map<String,Collection<Integer>> cityNameAndDocuments = new HashMap<>();

    /**
     * Constructor For The Class.
     * Uses relevant Files to find all cities, get their relevant documents from posting list and cityIndex
     * @param dictionary dictionary of indexed corpus (path)
     * @param postingFilePath posting file of indexed corpus (path)
     * @param cityIndexPath city index of indexed Corpus (path)
     * @throws IOException if cannot find one or more of the files
     */
    public CityFilter(Map<String, Pair<Integer, Integer>> dictionary, String postingFilePath, String cityIndexPath) throws IOException {
        setCityNameAndDocumentsFromCityIndex(cityIndexPath);
        setCityNameAndDocumentsFromDictionary(getRelevantDictionary(dictionary,cityNameAndDocuments.keySet()),postingFilePath);
        for (String s:cityNameAndDocuments.keySet()) {
            selectedCities.add(s);
        }
    }

    /**
     * Checks if all cities are selected
     * @return true if all cities are selected otherwise false
     */
    public boolean allSelected(){
        return selectedCities.size()==cityNameAndDocuments.keySet().size();
    }

    /**
     * Removes city from selected (if it is selected)
     * @param cityName
     */
    public void removeFromSelected(String cityName){
        if(selectedCities.contains(cityName))
            selectedCities.remove(cityName);
        System.out.println("ammount of cities: "+selectedCities.size());
    }

    /**
     * Adds city to selected cities
     * @param cityName city to add
     */
    public void addToSelected(String cityName){
        if(cityNameAndDocuments.keySet().contains(cityName)) {
            if (!selectedCities.contains(cityName))
                selectedCities.add(cityName);
        }
        else {
            if (selectedCities.contains(cityName))
                selectedCities.remove(cityName);
        }
    }


    private Map<String, Pair<Integer,Integer>> getRelevantDictionary(Map<String, Pair<Integer,Integer>> dictionary,Collection<String> cityNames){
        Map<String, Pair<Integer,Integer>> relevantDictionary = new HashMap<>();
        for (String cityName:cityNames) {
            String dictionaryEntryName=null;
            if(dictionary.containsKey(cityName))
                dictionaryEntryName = cityName;
            else if(dictionary.containsKey(cityName.toLowerCase()))
                dictionaryEntryName = cityName.toLowerCase();
            if(dictionaryEntryName!=null)
                relevantDictionary.put(cityName,dictionary.get(dictionaryEntryName));
        }
        return relevantDictionary;
    }

    /**
     * Adds Relevant Documents based on appearances in dictionary and posting path
     * @param relevantDictionary Dictionary that contains all cities
     * @param postingFilePath path to posting list
     * @throws IOException if file not found
     */
    private void setCityNameAndDocumentsFromDictionary(Map<String, Pair<Integer,Integer>> relevantDictionary,String postingFilePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(postingFilePath));


        PriorityQueue<Pair<String,Integer>> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(Pair::getValue));

        HashMap<Integer,Collection<String>> linesAndRelevantTerms = new HashMap<>();

        for (String cityName:relevantDictionary.keySet()) {
            priorityQueue.add(new Pair<>(cityName,relevantDictionary.get(cityName).getValue()));
        }
        int currentLineNum=0;
        String line=reader.readLine();
        while (!priorityQueue.isEmpty()){
            Pair<String,Integer> nextPair = priorityQueue.poll();
            while (currentLineNum<nextPair.getValue()){
                line=reader.readLine();
                currentLineNum++;
            }

            PostingList postingList = new PostingList(line);
            List<Integer> termDocs = new ArrayList<>();
            PostingEntry postingEntry = postingList.RemoveFirst();
            while(postingEntry!=null){
                termDocs.add(postingEntry.getDocID());
                postingEntry= postingList.RemoveFirst();
            }

            Collection<Integer> existingCollection = this.cityNameAndDocuments.get(nextPair.getKey());
            for (int i:termDocs) {
                if(!existingCollection.contains(i))
                    existingCollection.add(i);
            }
        }
    }

    /**
     * Gets All cities from city index
     * @param cityIndexPath path to city index
     * @throws IOException if file not found
     */
    private void setCityNameAndDocumentsFromCityIndex(String cityIndexPath) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(cityIndexPath));
        while ((line=reader.readLine())!=null){
            //get city name
            int indexOfSpace = line.indexOf(" ");
            String cityName = line.substring(0,indexOfSpace);
            line = line.substring(indexOfSpace+1);
            //remove currency and country
            indexOfSpace = line.indexOf(" ");
            line = line.substring(indexOfSpace+1);
            indexOfSpace = line.indexOf(" ");
            line = line.substring(indexOfSpace+1);

            //split by ' , ' . every document and locations of city
            String[] splitLine = line.split(",");
            Collection<Integer> appearancesOfCity = new HashSet<>();
            for (String cityAppearance:splitLine) {
                String[] splitAppearance = cityAppearance.split(":");
                appearancesOfCity.add(Integer.parseInt(splitAppearance[0]));
            }
            this.cityNameAndDocuments.put(cityName,appearancesOfCity);
        }
    }

    /**
     * Gets all the Ids of selected cities
     * @return all the Ids of selected cities
     */
    public Collection<Integer> getSelectedDocIDs(){
        HashSet<Integer> toReturn = new HashSet<>();
        for (String s:selectedCities) {
            Collection<Integer> relevantDocIDs = this.cityNameAndDocuments.get(s);
            for (int i:relevantDocIDs) {
                if(!toReturn.contains(i))
                    toReturn.add(i);
            }
        }
        return toReturn;
    }

    /**
     * Gets all city names regardless if they are selected or not
     * @return all city names regardless if they are selected or not
     */
    public List<String> getAllCityNames(){
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();
        priorityQueue.addAll(this.cityNameAndDocuments.keySet());
        List<String> toReturn = new ArrayList<>(priorityQueue.size());
        while (!priorityQueue.isEmpty())
            toReturn.add(priorityQueue.poll());
        return toReturn;
    }

    /**
     * Selects all cities or removes them all
     * @param selectAll true->select all, false->remove all
     */
    public void selectOrRemoveAll(boolean selectAll){
        if(selectAll){
            for (String s:cityNameAndDocuments.keySet())
                addToSelected(s);
        }
        else
            this.selectedCities.clear();
    }

    public Collection<Integer> getDocByCityName(String cityName){
        return this.cityNameAndDocuments.get(cityName);
    }

    /**
     *  Gets all selected cities
     * @return all selected cities
     */
    public Collection<String> getSelectedCities() {
        return selectedCities;
    }


}
