package gr.ictpro.jsalatas.gestures.ui.recorddata.controller;

import gr.ictpro.jsalatas.gestures.model.Point;
import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Recorder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RecordController implements Initializable {
    @FXML
    LineChart<Number, Number> chartAcceleration;

    private List<Point> points = new ArrayList<>();

    private XYChart.Series<Number, Number> seriesX;
    private XYChart.Series<Number, Number> seriesY;
    private XYChart.Series<Number, Number> seriesZ;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Recorder.setUi(this);

        seriesX = new XYChart.Series<>();
        seriesX.setName("X");
        seriesY = new XYChart.Series<>();
        seriesY.setName("Y");
        seriesZ = new XYChart.Series<>();
        seriesZ.setName("Z");

        chartAcceleration.getData().add(seriesX);
        chartAcceleration.getData().add(seriesY);
        chartAcceleration.getData().add(seriesZ);
    }

    public void addPoint(Point p) {
        points.add(p);
        if (points.size() > 51) {
            while (points.size() > 51) {
                points.remove(0);
            }
        }
        if (points.size() == 51) {
            updateChart();
        }
    }

    private void updateChart() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                // calc mean
                int counter = 0;
//                float averageX = 0;
//                float averageY = 0;
//                float averageZ = 0;
//                for (Point p : points) {
//                    averageX += p.getXa();
//                    averageY += p.getYa();
//                    averageZ += p.getZa();
//                    counter++;
//                }
//
//                averageX /= counter;
//                averageY /= counter;
//                averageZ /= counter;
//
//                double sdX = 0;
//                double sdY = 0;
//                double sdZ = 0;
//
//                // calc stdev
//                for (Point p : points) {
//                    sdX += (p.getXa() - averageX) * (p.getXa() - averageX);
//                    sdY += (p.getYa() - averageY) * (p.getYa() - averageY);
//                    sdZ += (p.getZa() - averageZ) * (p.getZa() - averageZ);
//                }
//
//                sdX = Math.sqrt(sdX / (1. * counter - 1.));
//                sdY = Math.sqrt(sdY / (1. * counter - 1.));
//                sdZ = Math.sqrt(sdZ / (1. * counter - 1.));
//
//
//                // add points to charts
//
//                counter = 0;
                seriesX.getData().clear();
                seriesY.getData().clear();
                seriesZ.getData().clear();
                for (int i = 1; i<points.size(); i++) {
                    Point p = points.get(i);
                    seriesX.getData().add(new XYChart.Data<>(counter, p.getXa()));
                    seriesY.getData().add(new XYChart.Data<>(counter, p.getYa()));
                    seriesZ.getData().add(new XYChart.Data<>(counter, p.getZa()));

                    counter++;
                }
                //System.out.println("----------------------------------------------------------------------------");
            }
        });

    }
}
