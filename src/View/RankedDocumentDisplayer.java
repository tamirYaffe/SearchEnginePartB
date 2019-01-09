package View;

import SearchEngineTools.Document;
import SearchEngineTools.ParsingTools.EntityParse;
import SearchEngineTools.ParsingTools.Term.ATerm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RankedDocumentDisplayer extends VBox {

    private List<Document> toDisplay;
    private List<Button> displayButtons=new ArrayList<>();
    private Collection<String> stopWords;



    public RankedDocumentDisplayer(List<Document> toDisplay,Collection<String> stopWords) {
        super();
        this.toDisplay = toDisplay;
        addCitiesToDisplay();
        this.setSpacing(20);
        this.stopWords=stopWords;
    }

    private void addCitiesToDisplay(){
        for (Document display:toDisplay){
            HBox hBox = new HBox();
            Label label = new Label();
            label.setText("Document: "+display.getDOCNO());
            Button entities = new Button("Show Entities");
            displayButtons.add(entities);
            entities.setOnAction(event->{
                onClickDisplayEntities(display);
            });
            hBox.getChildren().addAll(label,entities);
            Label empty = new Label();
            this.getChildren().add(hBox);
//            this.getChildren().addAll(label,entities, empty);
        }
    }

    private void onClickDisplayEntities(Document document){
        actionAllButtons(true);
        EntityParse entityParse = new EntityParse();
        entityParse.setStopWords(stopWords);
        Collection<ATerm> entities = entityParse.parseDocument(document.getDocLines());

        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().addAll(/*whatever you want to display*/);
        for (ATerm term:entities){
            HBox hBox = new HBox();
            Label label = new Label(term.getTerm()+", score:"+term.getOccurrences());
            hBox.getChildren().add(label);
            dialogVbox.getChildren().add(hBox);
        }
        Button close = new Button("close");
        close.setOnAction(event->{
            dialog.close();
        });
        Scene dialogScene = new Scene(dialogVbox, 550, 230);
        dialog.setScene(dialogScene);
        dialog.setTitle("Entities for document: "+document.getDOCNO());
        dialog.showAndWait();
        actionAllButtons(false);
    }
    private void actionAllButtons(boolean disable){
        for (Button b:displayButtons){
            b.setDisable(disable);
        }
    }

}
