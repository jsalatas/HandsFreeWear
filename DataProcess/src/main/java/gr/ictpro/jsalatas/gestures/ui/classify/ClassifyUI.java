package gr.ictpro.jsalatas.gestures.ui.classify;

import gr.ictpro.jsalatas.gestures.db.DB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClassifyUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("classify.fxml"));
        primaryStage.setTitle("Classification");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        DB.close();
    }

}
