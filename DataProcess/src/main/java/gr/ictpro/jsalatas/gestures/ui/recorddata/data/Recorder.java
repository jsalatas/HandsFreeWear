package gr.ictpro.jsalatas.gestures.ui.recorddata.data;

import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.model.Point;
import gr.ictpro.jsalatas.gestures.ui.recorddata.controller.RecordController;
import javafx.application.Platform;

public class Recorder {
    private static RecordController ui;
    private static int counter;



    public static void addPoint(long time, Integer x, Integer y, Integer z) {
        try {
            Point p = new Point(time, x, y, z, 0);
            //Platform.runLater(() ->  srv.save(p));
            DB.save(p);
            ui.addPoint(p);
            counter++;
            if(counter % 1000 == 0) {
                System.out.println("Total points: " + counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUi(RecordController ui) {
        Recorder.ui = ui;
    }
}
