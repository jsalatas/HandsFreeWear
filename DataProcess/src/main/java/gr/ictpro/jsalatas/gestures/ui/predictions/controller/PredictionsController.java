package gr.ictpro.jsalatas.gestures.ui.predictions.controller;

import gr.ictpro.jsalatas.gestures.export.Csv;
import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Record;
import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Transformer;
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
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.awt.Rectangle;
import java.awt.Color;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PredictionsController implements Initializable {

    private static final String MODEL = "../HandsFreeWear/wear/src/main/assets/frozen_model.pb";
    @FXML
    ChartViewer viewerAcceleration;

    @FXML
    ChartViewer viewerClassification;

    @FXML
    ScrollBar position;

    @FXML
    ScrollBar zoom;

    @FXML
    TextField txtWindowSize;

    @FXML
    Label lblPos;

    private JFreeChart chartAcceleration;

    private JFreeChart chartClassification;

    private Rectangle marker;

    private XYShapeAnnotation annotation;

    private int datasetSize = 0;

    private XYSeries seriesClassification;

    private Transformer transformer = new Transformer(Csv.WINDOW_SIZE);

    private Session session;

    private int ignoreClassificationCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            byte[] graphDef = Files.readAllBytes(Paths.get(MODEL));
            Graph g = new Graph();
            g.importGraphDef(graphDef);
            session = new Session(g);
        } catch (Exception e) {
            System.out.println("Model not found. No predictions will be available");
        }

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

        XYSeries seriesPredictions = new XYSeries("Predictions");
        classificationDataset.addSeries(seriesPredictions);

        for(int i=0;i<Csv.WINDOW_SIZE; i++) {
            seriesClassification.add(i, 0);
            seriesPredictions.add(i, 0);
        }
        Record record = transformer.getNextRecord(null);
        while (record != null) {
            int[] data = record.getData();
            seriesXa.add(datasetSize, data[0]);
            seriesYa.add(datasetSize, data[1]);
            seriesZa.add(datasetSize, data[2]);
            seriesClassification.add(datasetSize+25, record.getClassification());
            seriesPredictions.add(datasetSize+25, getPrediction(data));
            datasetSize++;
            if(record.getStep()>1) {
                for (int i=0; i< record.getStep() - 1; i++) {
                    seriesXa.add(datasetSize, data[i*3+3]);
                    seriesYa.add(datasetSize, data[i*3+4]);
                    seriesZa.add(datasetSize, data[i*3+5]);
                    seriesClassification.add(datasetSize+25, 0);
                    seriesPredictions.add(datasetSize+25, 0);
                    datasetSize++;
                }
            }
            record = transformer.getNextRecord(record);
        }

        printConfussionMatrix(seriesClassification, seriesPredictions);
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
                mouseEvent.consume();
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

    private void printConfussionMatrix(XYSeries seriesClassification, XYSeries seriesPredictions) {
        int size = 15;//seriesClassification.getItemCount();
        int[][] matrix = new int[size][size];

        for(int i=0; i<seriesClassification.getItemCount(); i++) {
            int prediction = (int) seriesPredictions.getDataItem(i).getYValue();
            int actual = (int) seriesClassification.getDataItem(i).getYValue();
            matrix[actual][prediction] = matrix[actual][prediction] + 1;
        }

        System.out.println("        0      1      2      3      4      5      6      7      8      9     10     11     12     13     14");
        for(int i=0; i<size; i++) {
            System.out.print(String.format("%2d", i));
            for(int j=0; j<size; j++) {
                System.out.print(matrix[i][j] ==0? "       " : String.format("%7d", matrix[i][j]));
            }
            System.out.println();
        }

        System.out.println(",0,1,2,3,4,5,6,7,8,9,10,11,12,13,14");
        for(int i=0; i<size; i++) {
            System.out.print(i);
            for(int j=0; j<size; j++) {
                System.out.print("," + (matrix[i][j] ==0? "" : matrix[i][j]));
            }
            System.out.println();
        }

    }

    private int getPrediction(int[] data) {
        int predicted = 0;
        if (session != null) {
            float[][] in = new float[1][data.length];
            for (int i = 0; i < data.length; i++) {
                in[0][i] = data[i];
            }
            Tensor<Float> input = (Tensor<Float>) Tensor.create(in);

            Tensor<Float> result = session.runner().feed("input_input_1", input).fetch("output_1/Softmax").run().get(0).expect(Float.class);
            final long[] rshape = result.shape();


            if (result.numDimensions() != 2 || rshape[0] != 1) {
                throw new RuntimeException(
                        String.format(
                                "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                Arrays.toString(rshape)));
            }

            float[] res = result.copyTo(new float[1][15])[0];
            float prob = 0.f;
            for (int i = 0; i < res.length; i++) {
                if (prob < res[i]) {
                    predicted = i;
                    prob = res[i];
                }
            }
        }
        return predicted;
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
        position.setMax(datasetSize - zoom.valueProperty().doubleValue());
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
