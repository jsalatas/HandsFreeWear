package gr.ictpro.jsalatas.handsfreewear.infer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by john on 1/1/18.
 */

public class Queue  {
    static final int MAX_ELEMENTS = 90;
    private final List<Float> elements = new ArrayList<>();

    private boolean disabled = true;

    private static Queue queue;

    private Queue() {
    }

    public static Queue getInstance() {
        if(queue == null) {
            queue = new Queue();
        }

        return queue;
    }

    void add(float[] values) {
        if(disabled) {
            return;
        }

        synchronized (elements) {
            if(elements.size()>MAX_ELEMENTS) {
                elements.remove(0);
                elements.remove(0);
                elements.remove(0);
            }

            elements.add(values[0]);
            elements.add(values[1]);
            elements.add(values[2]);
        }


        if(elements.size()>=MAX_ELEMENTS) {
            Infer.getInstance().inferGesture();
        }
    }


    float[] getRecord() {
        Float[] l;
        synchronized (elements) {
            if (elements.size() < MAX_ELEMENTS) {
                return null;
            }

            l = elements.subList(0, MAX_ELEMENTS).toArray(new Float[MAX_ELEMENTS]);
        }
        float[] res = new float[MAX_ELEMENTS];
        for (int i=0; i< res.length; i+=3) {
            res[i] = l[i];
            res[i+1] = l[i+1];
            res[i+2] = l[i+2];
        }

        return res;
    }

    public void reset() {
        synchronized (elements) {
            for(int i=0; i<elements.size(); i++) {
                elements.set(i, .0f);
            }
            State.getInstance().resetState();
        }
    }

    public void disable() {
        SensorReader.getInstance().disable();
        reset();
        disabled = true;
    }

    public void enable() {
        disabled = false;
        SensorReader.getInstance().enable();
    }

    public boolean isDisabled() {
        return disabled;
    }
}
