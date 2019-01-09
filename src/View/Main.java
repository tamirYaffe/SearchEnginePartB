package View;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import Model.Model;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Search Engine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("view.fxml").openStream());
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        //--------------
        Model model = new Model();
        View view = fxmlLoader.getController();
        model.addObserver(view);
        view.setStage(primaryStage);
        view.setModel(model);

        //--------------
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
