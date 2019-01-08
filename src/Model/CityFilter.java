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

public class CityFilter {

    public Collection<String> getSelectedCities() {
        return selectedCities;
    }

    private Collection<String> selectedCities=new HashSet<>();
    private Map<String,Collection<Integer>> cityNameAndDocuments = new HashMap<>();
   // private Map<String, Pair<Integer,Integer>> relevantDictionary;
//    private Collection<String> cityNames;

    public CityFilter(Map<String, Pair<Integer, Integer>> dictionary, String postingFilePath, String cityIndexPath) throws IOException {
        setCityNameAndDocumentsFromCityIndex(cityIndexPath);
        setCityNameAndDocumentsFromDictionary(getRelevantDictionary(dictionary,cityNameAndDocuments.keySet()),postingFilePath);
        for (String s:cityNameAndDocuments.keySet()) {
            selectedCities.add(s);
        }
    }
    
    public boolean allSelected(){
        return selectedCities.size()==cityNameAndDocuments.keySet().size();
    }
    
    public void removeFromSelected(String cityName){
        if(selectedCities.contains(cityName))
            selectedCities.remove(cityName);
        System.out.println("ammount of cities: "+selectedCities.size());
    }
    
    public void addToSelected(String cityName){
        if(cityNameAndDocuments.keySet().contains(cityName)) {
            if (!selectedCities.contains(cityName))
                selectedCities.add(cityName);
        }
        else {
            if (selectedCities.contains(cityName))
                selectedCities.remove(cityName);
        }
        System.out.println("ammount of cities "+selectedCities.size());
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



        /*Map<Integer,String> postingLinesWords = new HashMap<>();
        for (String cityName:relevantDictionary.keySet()) {
            postingLinesWords.putIfAbsent(relevantDictionary.get(cityName).getValue(),cityName);
        }
        BufferedReader reader = new BufferedReader(new FileReader(postingFilePath));
        String line;
        int index = 0;
        while ((line=reader.readLine())!=null){
            if(postingLinesWords.keySet().contains(index)){
                String[] splitLine = line.split(" ");
                Collection<Integer> docIDs = this.cityNameAndDocuments.get(postingLinesWords.get(index));
                for (int i = 0; i < splitLine.length; i+=2) {
                    int id = Integer.parseInt(splitLine[i]);
                    if(!docIDs.contains(id))
                        docIDs.add(id);
                }
            }
            index++;
        }*/
    }

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

    public Collection<Integer> getSelectedDocIDs(){
        HashSet<Integer> toReturn = new HashSet<>();
        for (String s:selectedCities) {
            if(s.equals("ABU")){
                System.out.println("all doc ids abu:");
                Collection<Integer> relevantDocIDs = this.cityNameAndDocuments.get(s);
                for (int i:relevantDocIDs) {
                    System.out.println(i);
                }
            }
            Collection<Integer> relevantDocIDs = this.cityNameAndDocuments.get(s);
            for (int i:relevantDocIDs) {
                if(!toReturn.contains(i))
                    toReturn.add(i);
            }
        }
        return toReturn;
    }

    public List<String> getAllCityNames(){
        PriorityQueue<String> priorityQueue = new PriorityQueue<>();
        priorityQueue.addAll(this.cityNameAndDocuments.keySet());
        List<String> toReturn = new ArrayList<>(priorityQueue.size());
        while (!priorityQueue.isEmpty())
            toReturn.add(priorityQueue.poll());
        return toReturn;
    }

    public void selectOrRemoveAll(boolean selectAll){
        if(selectAll){
            for (String s:cityNameAndDocuments.keySet())
                addToSelected(s);
        }
        else
            this.selectedCities.clear();
        System.out.println("Select All Action: "+selectAll+"\n" +
                "ammount of cities: " + selectedCities.size());
    }

    public Collection<Integer> getDocByCityName(String cityName){
        return this.cityNameAndDocuments.get(cityName);
    }


}
