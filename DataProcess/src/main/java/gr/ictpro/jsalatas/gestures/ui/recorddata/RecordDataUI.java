package gr.ictpro.jsalatas.gestures.ui.recorddata;

import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.ui.recorddata.server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RecordDataUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("record.fxml"));
        primaryStage.setTitle("Gesture Recorder");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(400);
        primaryStage.setMaxWidth(800);
        primaryStage.setMaxHeight(400);
        primaryStage.setResizable(false);
        primaryStage.setMaximized(true);
        primaryStage.show();

        Server.startServer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        Server.stopServer();
        DB.close();
    }


}
