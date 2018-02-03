package gr.ictpro.jsalatas.gestures.ui.classify.controller;

import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.model.Point;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Rectangle;
import java.awt.Color;
import java.net.URL;
import java.util.*;

public class ClassifyController implements Initializable {

    @FXML
    ChartViewer viewerAcceleration;

    @FXML
    ChartViewer viewerClassification;

    @FXML
    ScrollBar position;

    @FXML
    ScrollBar zoom;

    @FXML
    TextField txtClassification;

    @FXML
    TextField txtWindowSize;

    @FXML
    Label lblPos;

    private final List<Point> points = DB.getPoints();

    private JFreeChart chartAcceleration;

    private JFreeChart chartClassification;

    private Rectangle marker;

    private boolean isDragging = false;

    private XYShapeAnnotation annotation;

    private int startX = -1;

    private XYSeries seriesClassification;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        XYSeriesCollection accelerometerDataset = new XYSeriesCollection();
        XYSeries seriesXa = new XYSeries("X");
        XYSeries seriesYa = new XYSeries("Y");
        XYSeries seriesZa = new XYSeries("Z");

        accelerometerDataset.addSeries(seriesXa);
        accelerometerDataset.addSeries(seriesYa);
        accelerometerDataset.addSeries(seriesZa);

        XYSeriesCollection classificationDataset = new XYSeriesCollection();
        seriesClassification = new XYSeries("Actual");
        classificationDataset.addSeries(seriesClassification);

        int counter = 0;
        for (Point p : points) {
            seriesXa.add(counter, p.getXa());
            seriesYa.add(counter, p.getYa());
            seriesZa.add(counter, p.getZa());
            seriesClassification.add(counter, p.getClassification());
            counter++;
        }

        chartAcceleration = ChartFactory.createXYLineChart("Accelerometer", null, null, accelerometerDataset);
        chartAcceleration.getXYPlot().setBackgroundAlpha(0);
        chartAcceleration.getXYPlot().setDomainGridlinePaint(Color.GRAY);
        chartAcceleration.getXYPlot().getDomainAxis().setAutoRange(false);
        viewerAcceleration.setChart(chartAcceleration);

        chartClassification = ChartFactory.createXYLineChart("", null, null, classificationDataset);
        chartClassification.getXYPlot().setBackgroundAlpha(0);
        chartClassification.getXYPlot().setDomainGridlinePaint(Color.GRAY);
        chartClassification.getXYPlot().setRangeGridlinePaint(Color.GRAY);
        chartClassification.getXYPlot().setDomainGridlinesVisible(true);
        chartClassification.getXYPlot().getDomainAxis().setAutoRange(false);
        chartClassification.removeLegend();

        chartClassification.getXYPlot().getRangeAxis().setAutoRange(false);
        chartClassification.getXYPlot().getRangeAxis().setLowerBound(-1);
        chartClassification.getXYPlot().getRangeAxis().setUpperBound(15);

        viewerClassification.setChart(chartClassification);


        zoom.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                adjustPosition();
            }
        });

        position.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                Platform.runLater(() -> refreshChart());
            }
        });

        zoom.valueProperty().setValue(100);
        adjustPosition();

        viewerAcceleration.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hideMarker();
            }
        });

        viewerAcceleration.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                event.consume();
            }
        });


        viewerAcceleration.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                int x = (int) chartAcceleration.getXYPlot().getDomainAxis().java2DToValue(event.getX(), viewerAcceleration.getCanvas().findDataArea(new java.awt.Point((int) event.getX(), (int) event.getY())), RectangleEdge.BOTTOM);
                setMarker(x);
            }
        });

        viewerAcceleration.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                updateClassification();
            }
        });

        viewerClassification.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                event.consume();
            }
        });
        viewerClassification.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mouseEvent.consume();
            }
        });

        marker = new Rectangle(0, -10000, 0, 100000);

    }

    private void updateClassification() {
        if (!txtClassification.getText().isEmpty()) {
            int classification = Integer.parseInt(txtClassification.getText());
            int from = (int) marker.getX();
            int to = from + (int) marker.getWidth();
            DB.updateClassification(from, to, classification);
            for (int i = from; i <= to; i++) {
                seriesClassification.update(i, classification);
            }
        }
    }

    private void updateAnnotations() {
        if (annotation != null) {
            chartAcceleration.getXYPlot().removeAnnotation(annotation);
            chartClassification.getXYPlot().removeAnnotation(annotation);
        }
        if (marker.getWidth() > 0) {
            annotation = new XYShapeAnnotation(marker, null, null, new Color(0, 0, 0, 20));
            chartAcceleration.getXYPlot().addAnnotation(annotation);
            chartClassification.getXYPlot().addAnnotation(annotation);
        }
    }

    private void hideMarker() {
        lblPos.setText("");
        marker.setSize(0, (int) marker.getHeight());
        updateAnnotations();
    }

    private void setMarker(int x) {
        marker.setLocation(x, (int) marker.getY());
        int width = 20 - 1;
        lblPos.setText(x + "");
        try {
            width = Integer.parseInt(txtWindowSize.getText()) - 1;
        } catch (NumberFormatException e) {
            // do nothing
        }
        marker.setSize(width, (int) marker.getHeight());
        updateAnnotations();
    }

    private void adjustPosition() {
        position.setMin(0);
        position.setMax(points.size() - zoom.valueProperty().doubleValue());
        position.setBlockIncrement(zoom.valueProperty().doubleValue());
        position.setVisibleAmount(zoom.valueProperty().doubleValue());

        Platform.runLater(() -> refreshChart());
    }

    private void refreshChart() {
        chartAcceleration.getXYPlot().getDomainAxis().setAutoRange(false);
        chartAcceleration.getXYPlot().getDomainAxis().setLowerBound(position.getValue());
        chartAcceleration.getXYPlot().getDomainAxis().setUpperBound(position.getValue() + position.getVisibleAmount());
        Range r = getDomainRange((int) position.getValue(), (int) (position.getValue() + position.getVisibleAmount()), (XYSeriesCollection) chartAcceleration.getXYPlot().getDataset());
        chartAcceleration.getXYPlot().getRangeAxis().setAutoRange(false);
        chartAcceleration.getXYPlot().getRangeAxis().setLowerBound(r.getLowerBound() * 1.1);
        chartAcceleration.getXYPlot().getRangeAxis().setUpperBound(r.getUpperBound() * 1.1);

        chartClassification.getXYPlot().getDomainAxis().setAutoRange(false);
        chartClassification.getXYPlot().getDomainAxis().setLowerBound(position.getValue());
        chartClassification.getXYPlot().getDomainAxis().setUpperBound(position.getValue() + position.getVisibleAmount());

    }

    private Range getDomainRange(int start, int end, XYSeriesCollection dataset) {
        int lower = Integer.MAX_VALUE;
        int upper = Integer.MIN_VALUE;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            XYSeries series = dataset.getSeries(i);
            for (int j = start; j < end; j++) {
                int value = (int) series.getDataItem(j).getYValue();
                if (value > upper) {
                    upper = value;
                }
                if (value < lower) {
                    lower = value;
                }
            }
        }
        return new Range(lower, upper);

    }


}
