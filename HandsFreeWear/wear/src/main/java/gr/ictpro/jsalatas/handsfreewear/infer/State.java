package gr.ictpro.jsalatas.handsfreewear.infer;

import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;
import gr.ictpro.jsalatas.handsfreewear.application.Settings;
import gr.ictpro.jsalatas.handsfreewear.ui.AccessibilityOverlay;

/**
 * Created by john on 1/1/18.
 */

class State {

    private static State state;
    private static final int MAX_STATE_COUNTER = 5;
    private final Settings settings;
    private final List<Integer> states = new ArrayList<>();


    private State() {
        settings = HandsFreeWearApplication.getSettings();
    }

    static State getInstance() {
        if (state == null) {
            state = new State();
        }

        return state;
    }

    private int lastState = 0;
    void toState(int s) {

        states.add(s);
        if(states.size()> MAX_STATE_COUNTER) {
            states.remove(0);
        }
        int currentState = getCurrentState(settings.getAccuracy());
        if(lastState != currentState) {
            AccessibilityOverlay.getInstance().setGesture(currentState);
        }
        lastState = currentState;
    }

    private int getCurrentState(int accuracy) {
        SparseIntArray statesMap = new SparseIntArray();
        for(Integer state : states) {
            if(state ==0) {
                continue;
            }

            statesMap.put(state, statesMap.get(state) + 1);
        }



        for(int i = 0; i < statesMap.size(); i++) {
            int key = statesMap.keyAt(i);
            if(statesMap.get(key) >= accuracy && key != 0) {
                return key;
            }
        }

        return 0;
    }

    public void resetState() {
        states.clear();
    }

}