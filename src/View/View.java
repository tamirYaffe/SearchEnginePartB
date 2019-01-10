package View;

import Model.Model;
import SearchEngineTools.Document;
import SearchEngineTools.Indexer;
import SearchEngineTools.ParsingTools.TokenList.TextTokenList;
import SearchEngineTools.Ranker.Ranker;
import SearchEngineTools.ReadFile;
import SearchEngineTools.datamuse.DatamuseQuery;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * A controller class for view.xml
 */
public class View implements Observer{
    private Stage primaryStage;
    private String fileSeparator=System.getProperty("file.separator");
    private boolean useStemming=false;
    private int maxSynonyms = 2;
    private Model model;




    //fxml widgets
    public TextField tf_corpusPath;
    public TextField tf_postingListPath;
    public Button btn_corpusFileSystem;
    public Button btn_postingListFileSystem;
    public CheckBox cb_useStemming;
    public Button btn_startIndex;
    public Button btn_loadDictionary;
    public Button btn_showDictionary;
    public Button btn_deleteAll;
    public Menu menu_languages;
    public TextField tf_naturalLanguageQuery;
    public Button btn_naturalLanguageQuery;
    public Button btn_fileQuery;
    public Button btn_queryFilesPath;
    public CheckBox cb_useSemantics;
    public CheckBox cb_spellCheck;
    public TextField tf_queryFilePath;
    public Button btn_queryResultFilesPath;
    public TextField tf_queryResultFilePath;
    public Button btn_filterByCities;
    public JTextArea jTextArea;



    //<editor-fold desc="Setters">
    public void setStage(Stage primaryStage) {
        this.primaryStage=primaryStage;
    }

    public void setModel(Model model) {
        this.model = model;
    }
    //</editor-fold>

    /**
     * Opens the file system for the user to choose the corpus path.
     */
    public void onClickCorpusFileSystem(){
        onClickOpenFileSystem(tf_corpusPath);
    }

    /**
     * opens file system and sets selected folder path to textfield
     * @param pathTextField
     */
    private void onClickOpenFileSystem(TextField pathTextField){
        actionAllButtons(true);
        String selectedDirectory = openFileSystem();
        if(selectedDirectory!=null)
            pathTextField.setText(selectedDirectory);
        actionAllButtons(false);
    }


    /**
     * Opens the file system for the user to choose the posting files path.
     */
    public void onClickPostingListFileSystem(){
        onClickOpenFileSystem(tf_postingListPath);
    }

    /**
     * opens file system to locate query file path
     */
    public void onClickQueryFileSystem(){
        onClickChooseFile(tf_queryFilePath);
    }

    /**
     * opens file system and chooses file. place path of file in text field
     * @param pathTextField textfield to get path of
     */
    private void onClickChooseFile(TextField pathTextField){
        actionAllButtons(true);
        final FileChooser fileChooser = new FileChooser();
        File selectedFile =fileChooser.showOpenDialog(primaryStage);
        if(selectedFile==null){
            actionAllButtons(false);
            return;
        }
        String path = selectedFile.getAbsolutePath();
        if(path!=null)
            pathTextField.setText(path);
        actionAllButtons(false);
    }

    /**
     * turns user text into list of words. used in construction natural language displayer object
     * @return
     */
    private List<String> getNaturalLanguageText() {
        String queryFieldText = this.tf_naturalLanguageQuery.getText();
        List<String> toReturn = new ArrayList<>();
        TextTokenList textTokenList = new TextTokenList();
        List<String> queryAsList = new ArrayList<>();
        queryAsList.add(queryFieldText);
        Collection<Character> currencySymbols = new HashSet<>();
        currencySymbols.add('$');
        Collection<Character> delimitersToSplitWordBy = new ArrayList<>();
        delimitersToSplitWordBy.add('-');
        textTokenList.initialize(queryAsList,currencySymbols,delimitersToSplitWordBy,new ArrayList<>());
        while (!textTokenList.isEmpty()){
            toReturn.add(textTokenList.pop().getTokenString());
        }
        return toReturn;
    }


    /**
     * Starting to index in a new thread.
     */
    public void onClickStartIndex(){
        actionAllButtons(true);
        //check errors
        if(tf_corpusPath.getText().length()==0 || tf_postingListPath.getText().length()==0){
            displayErrorMessage("Add path to input corpus and output posting files");
            actionAllButtons(false);
            return;
        }
        if(cb_useStemming.isSelected())
            useStemming=true;
        else
            useStemming=false;
        deletePostingFiles(useStemming);
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                String msg=model.startIndex(tf_corpusPath.getText(),tf_postingListPath.getText(),useStemming);
                Platform.runLater(()->{
                    displayEndOfRunMessage(msg);
                });
                return null;
            }
        };
        Thread startIndex=new Thread(task);
        startIndex.setDaemon(true);
        startIndex.start();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Started Indexing...");
        alert.setHeaderText(null);
        alert.setContentText("Please wait until buttons become enable again.");
        alert.showAndWait();
    }

    /**
     * Loads the dictionary from the input posting files path to the indexer memory.
     * dictionary load is determine by the use stemming check box. Restarts city index
     */
    public void onClickLoadDictionary(){
        actionAllButtons(true);
        Map<String,Pair<Integer,Integer>> dictionary = getDictionary();
        //load dictionary to index dictionary
        model.loadDictionary(dictionary);
        model.setCityFilter(null);
        if(model.getDictionarySize()!=0){
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Dictionary Loaded");
            success.setHeaderText("Dictionary successfully loaded");
            success.showAndWait();
        }
        else {
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setHeaderText("counld not load dictionary. Make sure the posting list path you inserted is the correct one");
            failure.setTitle("Could Not Load Dictionary");
            failure.showAndWait();
        }
        actionAllButtons(false);
    }


    private Map<String,Pair<Integer,Integer>> getDictionary(){
        Map<String, Pair<Integer,Integer>> dictionary=new HashMap<>();
        int postingListPointer=0;
        try {
            String fileName;
            if(cb_useStemming.isSelected())
                fileName="dictionaryStemming.txt";
            else
                fileName="dictionary.txt";
            BufferedReader reader = new BufferedReader(new FileReader(tf_postingListPath.getText()+fileSeparator+fileName));
            String line;
            while (( line=reader.readLine())!=null){
                dictionary.put(line.split(":")[0],new Pair<>(Integer.valueOf(line.split(":")[1]),postingListPointer++));
            }
        } catch (IOException e) {
            displayErrorMessage("load failed");
        }
        return dictionary;
    }


    /**
     * Shows the dictionary to the user.
     * dictionary showing is determine by the use stemming check box.
     */
    public void onClickShowDictionary(){
        actionAllButtons(true);
        try {
            String fileName;
            if(useStemming)
                fileName="dictionaryStemming.txt";
            else
                fileName="dictionary.txt";
            String filePath=tf_postingListPath.getText()+fileSeparator+fileName;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            jTextArea=new JTextArea();
            jTextArea.read(reader,null);
            jTextArea.setEditable(false);
            JFrame frame = new JFrame("TextArea Load");
            frame.getContentPane().add( new JScrollPane(jTextArea));
            frame.pack();
            frame.setLocationRelativeTo( null );
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch (IOException e) {
            e.printStackTrace();
        }
        actionAllButtons(false);
    }

    /**
     * Deletes all posting files and in memory program vars.
     */
    public void onClickSDeleteAll(){
        actionAllButtons(true);
        if(tf_postingListPath.getText().length()==0){
            displayErrorMessage("please enter posting list path from which to delete");
        }
        model.deleteAll(tf_postingListPath.getText());
        deletePostingFiles(true);
        deletePostingFiles(false);
        menu_languages.getItems().clear();
        actionAllButtons(false);
    }



    //private methods

    /**
     * Adds the corpus files languages to the menu.
     */
    private void addLanguages() {
        Collection<String>languages=model.getLanguages();
        ArrayList<MenuItem>items=new ArrayList<>();
        if(languages==null)
            return;
        for(String language:languages)
            items.add(new MenuItem(language));
        menu_languages.getItems().addAll(items);
    }

    /**
     * Opens the file system and returns the user chosen path.
     * @return
     */
    private String openFileSystem(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(primaryStage);
        if(selectedDirectory==null)
            return null;
        return selectedDirectory.getAbsolutePath();
    }

    /**
     * Disable or enable all buttons according to the input disable.
     * @param disable- the action we wish to perform on the buttons.
     */
    private void actionAllButtons(boolean disable) {
        tf_corpusPath.setEditable(!disable);
        tf_postingListPath.setEditable(!disable);
        btn_corpusFileSystem.setDisable(disable);
        btn_postingListFileSystem.setDisable(disable);
        cb_useStemming.setDisable(disable);
        btn_startIndex.setDisable(disable);
        btn_loadDictionary.setDisable(disable);
        btn_showDictionary.setDisable(disable);
        btn_deleteAll.setDisable(disable);
        menu_languages.setDisable(disable);
        tf_naturalLanguageQuery.setEditable(!disable);
        btn_naturalLanguageQuery.setDisable(disable);
        btn_fileQuery.setDisable(disable);
        btn_queryFilesPath.setDisable(disable);
        cb_useSemantics.setDisable(disable);
        cb_spellCheck.setDisable(disable);
        tf_queryFilePath.setEditable(!disable);
        btn_queryResultFilesPath.setDisable(disable);
        tf_queryResultFilePath.setEditable(!disable);
        btn_filterByCities.setDisable(disable);
    }

    /**
     * Displaying an error message for the user, with given msg.
     * @param msg- the message we wish to present.
     */
    private void displayErrorMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Ooops, there was an error!");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void displayEndOfRunMessage(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Finished indexing!");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Delete all posting files.
     */
    private void deletePostingFiles(boolean useStemming) {
        String path=tf_postingListPath.getText();
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

    }

    @Override
    public void update(Observable o, Object arg) {
        addLanguages();
        actionAllButtons(false);

    }

    private void showNoQueryResultFileAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Path To Results file found");
        alert.setContentText("Please make sure path to results file is correctly inserted ins specified text box");
        alert.setHeaderText("");
        alert.showAndWait();
    }

    private void showNoQueryFoundAlert(String contentText){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No query found");
        alert.setContentText(contentText);
        alert.setHeaderText("");
        alert.showAndWait();
    }

    /**
     * Displays window to start query. Displays suggested query and runs user's natural language query as needed
     */
    public void onClickNaturalLanguageQuery(){
        actionAllButtons(true);

        if(tf_queryResultFilePath.getText().equals("")){
            showNoQueryResultFileAlert();
            actionAllButtons(false);
            return;
        }

        if(tf_naturalLanguageQuery.getText()==null ||tf_naturalLanguageQuery.getText().equals("")){
            showNoQueryFoundAlert("Please type a query");
            actionAllButtons(false);
            return;
        }
        if(tf_corpusPath.getText().length()==0){
            displayErrorMessage("Add path to input corpus");
            actionAllButtons(false);
            return;
        }
        if(model.getDictionarySize()==0){
            displayErrorMessage("No Dictionary Loaded");
            actionAllButtons(false);
            return;
        }
        final Stage dialog = new Stage();
//        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().addAll(/*whatever you want to display*/);

        Label label = new Label("Please make sure that you typed the correct query");
        dialogVbox.getChildren().addAll(label);


        HBox hBox_suggested = new HBox();
        Label suggest = new Label("May we suggest: ");
        hBox_suggested.getChildren().addAll(suggest);
        NaturalQueryDisplayer naturalQueryDisplayer = new NaturalQueryDisplayer(getNaturalLanguageText());
        hBox_suggested.getChildren().addAll(naturalQueryDisplayer);
        Button btn_fixSuggestion = new Button("This is what I meant");
        btn_fixSuggestion.setOnAction(e-> {
             queryFromUser(naturalQueryDisplayer.getQuery());
            dialog.close();
        });
        hBox_suggested.getChildren().addAll(btn_fixSuggestion);

        HBox hBox_CorrectQuery = new HBox();
        Label askUser = new Label("are you sure that"+'"'+' '+ tf_naturalLanguageQuery.getText()+'"'+" is the correct query");
        hBox_CorrectQuery.getChildren().addAll(askUser);
        Button btn_userIsSure = new Button("Yes, I'm Sure");
        btn_userIsSure.setOnAction(e-> {
            queryFromUser(tf_naturalLanguageQuery.getText());
            dialog.close();
        });
        hBox_CorrectQuery.getChildren().addAll(btn_userIsSure);

        Button btn_cancel = new Button("Cancel");

        btn_cancel.setOnAction(event -> {
            dialog.close();
        });

        dialogVbox.getChildren().add(hBox_CorrectQuery);
        dialogVbox.getChildren().add(hBox_suggested);
        dialogVbox.getChildren().add(btn_cancel);

        Scene dialogScene = new Scene(dialogVbox, 550, 230);
        dialog.setScene(dialogScene);
        dialog.setTitle("Double Check Your Query");
        dialog.showAndWait();
        actionAllButtons(false);
    }


    private void queryFromUser(String query){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Query "+'"'+query+'"');
        alert.setHeaderText("After OK pressed your results will be displayed shortly");
        alert.showAndWait();
        List<Document> rankedDocuments =  model.queryNaturalLanguage(query,tf_postingListPath.getText(),cb_useStemming.isSelected(),cb_useSemantics.isSelected(),tf_queryResultFilePath.getText(),tf_corpusPath.getText());
        RankedDocumentDisplayer rankedDocumentDisplayer = new RankedDocumentDisplayer(rankedDocuments,model.getStopWords(tf_corpusPath.getText()));

        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        Button close = new Button("Close");
        close.setOnAction(event -> {dialog.close();});
        rankedDocumentDisplayer.getChildren().addAll(close);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(rankedDocumentDisplayer);
        scrollPane.setPannable(true);

        Scene dialogScene = new Scene(scrollPane, 550, 700);
        dialog.setScene(dialogScene);
        dialog.setTitle("Query Results from query: "+query);
        dialog.showAndWait();
    }


    /**
     * Open city filter and allow user to filter results by cities
     * @param actionEvent
     */
    public void onClickFilterByCities(ActionEvent actionEvent) {
        actionAllButtons(true);
        try {
            VBox vBox = new VBox(10);
            CheckBox cb_selectAll = new CheckBox("select all");
            List<String> allCityNames;
            Collection<String> selectedCityNames;
            List<CheckBox> allCheckBoxes = new ArrayList<>();
            final Stage dialog = new Stage();
            dialog.initModality(Modality.NONE);
            allCityNames = model.getAllCityNames(tf_postingListPath.getText() +fileSeparator+ "postingLists.txt", tf_postingListPath.getText() +fileSeparator+ "cityIndex.txt");
            selectedCityNames = model.getAllSelectedCityNames();
            ListView listView = new ListView();
            listView.setDisable(false);
            for (String cityName:allCityNames) {
                CheckBox cityCheckBox = new CheckBox(cityName);
                boolean selectBox = selectedCityNames.contains(cityName);
                cityCheckBox.setSelected(selectBox);
                cityCheckBox.setOnAction(event -> {
                    if(!cityCheckBox.isSelected()) {
                        model.removeCity(cityName);
                        cb_selectAll.setSelected(false);
                    }
                    else
                        model.addCity(cityName);
                    System.out.println("city: "+cityName+" is "+(model.getAllSelectedCityNames().contains(cityName)?"selected":"not selected"));
                });
                allCheckBoxes.add(cityCheckBox);
                listView.getItems().add(cityCheckBox);
            }
            HBox cityList = new HBox(listView);
            HBox hbox_selectAll = new HBox();
            cb_selectAll.setSelected(model.allCitiesSelected());
            cb_selectAll.setOnAction(e->{
                boolean action = cb_selectAll.isSelected();
                for (int i = 0; i < allCheckBoxes.size(); i++) {
                    allCheckBoxes.get(i).setSelected(action);
                }
                model.selecAllCities(action);
            });
            hbox_selectAll.getChildren().add(cb_selectAll);
            vBox.getChildren().addAll(hbox_selectAll);
            cityList.setPrefHeight(500);
            cityList.setPrefWidth(550);
            vBox.getChildren().addAll(cityList);
            Scene dialogScene = new Scene(vBox, 550, 650);
            dialog.setScene(dialogScene);
            dialog.setTitle("Filter By Cities");
            dialog.showAndWait();
            actionAllButtons(false);
        }
        catch (Exception e){
            Alert failed = new Alert(Alert.AlertType.ERROR);
            failed.setTitle("cannot load city filter");
            failed.setHeaderText("cannot open file: "+tf_postingListPath.getText() + fileSeparator + "postingLists.txt\n" +
                    "or cannot find file: "+tf_postingListPath.getText() + fileSeparator + "cityIndex.txt");
            failed.setContentText("make sure the path to the specified files is specified in the correct text box");
            failed.showAndWait();
            actionAllButtons(false);
            return;
        }
    }

    /**
     * run queries on all querries in a file
     */
    public void onClickQueryFiles(){
        actionAllButtons(true);
        if(tf_queryFilePath.getText().length()==0){
            showNoQueryFoundAlert("Please select a query file");
            actionAllButtons(false);
            return;
        }

        if(tf_queryResultFilePath.getText().length()==0){
            showNoQueryResultFileAlert();
            actionAllButtons(false);
            return;
        }

        if(tf_corpusPath.getText().length()==0){
            displayErrorMessage("Add path to corpus input");
            actionAllButtons(false);
            return;
        }
        if(model.getDictionarySize()==0){
            displayErrorMessage("no dicitonary loaded");
            actionAllButtons(false);
            return;
        }
        Alert startedQueries = new Alert(Alert.AlertType.INFORMATION);
        startedQueries.setTitle("Running queries from file "+'"'+tf_queryFilePath.getText()+'"');
        startedQueries.setHeaderText("Your results will be displayed shortly");
        startedQueries.showAndWait();
        try {
            model.queryFromFile(tf_queryFilePath.getText(),tf_postingListPath.getText(),cb_spellCheck.isSelected(),cb_useStemming.isSelected(),cb_useSemantics.isSelected(),tf_queryResultFilePath.getText(),tf_corpusPath.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Check results in "+tf_queryResultFilePath.getText()+fileSeparator+"results.txt");
            alert.setHeaderText("Ran queries from file");
            alert.showAndWait();
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("unable to process queries");
            alert.setHeaderText("please check that all paramaters are correctly inserted");
            alert.setContentText("check the following:\n\n" +
                    "1) path to the posting list is correctly inserted\n\n" +
                    "(looking for file in: "+tf_postingListPath.getText()+"\n" +
                    "2) path to query file is correctly inserted\n" +
                    "looking for file: "+tf_queryFilePath.getText()+"\n\n" +
                    "3) path to results file is correctly inserted\n" +
                    "looking for file: "+tf_queryResultFilePath.getText()+"\n\n" +
                    "4) dictionary is loaded");
            alert.showAndWait();
        }
        actionAllButtons(false);
    }

    /**
     * open file system and allow user to choose location of results file
     * @param actionEvent
     */
    public void onClickQueryResultFileSystem(ActionEvent actionEvent) {
        onClickOpenFileSystem(tf_queryResultFilePath);
    }
}
