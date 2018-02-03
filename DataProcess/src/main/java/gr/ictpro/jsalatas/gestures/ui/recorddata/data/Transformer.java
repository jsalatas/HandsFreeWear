package gr.ictpro.jsalatas.gestures.ui.recorddata.data;

import gr.ictpro.jsalatas.gestures.db.DB;
import gr.ictpro.jsalatas.gestures.model.Point;

import java.util.List;

public class Transformer {
    private int windowSize;
    private static List<Point> points = DB.getPoints();
    private int pos = 0;

    public Transformer(int windowSize) {
        this.windowSize = windowSize;
    }

    public Record getNextRecord(Record previousRecord) {
        pos += previousRecord ==null? 0 : previousRecord.getStep();
        if(pos >= points.size() - windowSize) {
            return null;
        }

        Record record = new Record(windowSize);
            boolean started = false;
            boolean ended = false;
            int startClassification = points.get(pos).getClassification();
            int endClassification = points.get(pos + windowSize).getClassification();
            boolean startsInNoise = startClassification < 1;
            boolean endsInNoise = endClassification < 1;
            int index = 0;
            for (int j = pos; j < pos+ windowSize; j++) {
                Point p0 = points.get(j);
                record.add(index, p0.getXa());
                record.add(index+1, p0.getYa());
                record.add(index+2, p0.getZa());
                index+=3;
                if (p0.getClassification() > 0) {
                    if (!started) {
                        started = true;
                        record.setClassification(p0.getClassification());
                    } else {
                        if (ended) {
                            record.setClassification(0);
                        }
                    }
                } else {
                    if (started) {
                        ended = true;
                    }
                }
            }
            if ((started && !(ended || endsInNoise)) || !startsInNoise || !endsInNoise) {
                record.setClassification(0);
            }
            if (startClassification != 0 && endClassification != 0) {
                record.setStep(0);
                for (int j = pos; j < pos + windowSize; j++) {
                    if (points.get(j).getClassification() == 0) {
                        break;
                    }
                    record.setStep(record.getStep()+1);
                }
            } else {
                record.setStep(1);
            }
            return record;

    }
}
