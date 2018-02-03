package gr.ictpro.jsalatas.gestures.ui.predictions;

import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.ui.recorddata.server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PredictionsUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("predictions.fxml"));
        primaryStage.setTitle("Predictions");
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
