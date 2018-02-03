package gr.ictpro.jsalatas.gestures.export;

import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Record;
import gr.ictpro.jsalatas.gestures.ui.recorddata.data.Transformer;

import java.io.*;
import java.util.GregorianCalendar;
import java.util.Random;

public class Csv {
    public static final int WINDOW_SIZE = 30;


    public static void main(String[] args) {

        Random r = new Random(GregorianCalendar.getInstance().getTimeInMillis());
        try {
            PrintWriter train = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("../train/data/data.csv")), "UTF-8"));

            writeHeader(train);

            Transformer transformer = new Transformer(WINDOW_SIZE);

            Record record = transformer.getNextRecord(null);
            while (record != null) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < record.getData().length; i++) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(",");
                    }
                    sb.append(record.getData()[i]);
                }

                sb.append(",");
                sb.append(record.getClassification());
                train.println(sb.toString());

                record = transformer.getNextRecord(record);
            }


            train.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeHeader(PrintWriter writer) {
        for (int i = 0; i < WINDOW_SIZE; i++) {
            writer.print("xa" + i + ",");
            writer.print("ya" + i + ",");
            writer.print("za" + i + ",");
        }
        writer.println("classification");
    }
}
