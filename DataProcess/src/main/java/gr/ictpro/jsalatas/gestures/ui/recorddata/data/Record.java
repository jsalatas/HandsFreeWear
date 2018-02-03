package gr.ictpro.jsalatas.gestures.ui.recorddata.data;

public class Record {
    private int[] data;
    private int classification;
    private int step;

    public Record(int windowSize) {
        data = new int[windowSize * 3];
    }

    public int[] getData() {
        return data;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification(int classification) {
        this.classification = classification;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void add(int index, int value) {
        data[index] = value;
    }
}
