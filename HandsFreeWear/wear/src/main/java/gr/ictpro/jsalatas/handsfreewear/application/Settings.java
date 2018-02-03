package gr.ictpro.jsalatas.handsfreewear.application;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by john on 1/6/18.
 */

public class Settings {
    private static final int DEFAULT_ACCURACY = 3;
    private static final boolean DEFAULT_VIBRATE = false;
    private static final boolean DEFAULT_HIGHLIGHT_SELECTED = true;

    private static final String KEY_ACCURACY = "accuracy";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_HIGHLIGHT_SELECTED = "highlight";

    private final SharedPreferences prefs;

    Settings() {
        prefs = PreferenceManager.getDefaultSharedPreferences(HandsFreeWearApplication.getContext());
    }

    public void setAccuracy(int accuracy) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ACCURACY, accuracy);
        editor.apply();
    }

    public int getAccuracy() {
        return prefs.getInt(KEY_ACCURACY, DEFAULT_ACCURACY);
    }

    public void setVibrate(boolean vibrate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_VIBRATE, vibrate);
        editor.apply();
    }

    public boolean getVibrate() {
        return prefs.getBoolean(KEY_VIBRATE, DEFAULT_VIBRATE);
    }

    public void setHighlightSelected(boolean highlightSelected) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_HIGHLIGHT_SELECTED, highlightSelected);
        editor.apply();
    }

    public boolean getHighlightSelected() {
        return prefs.getBoolean(KEY_HIGHLIGHT_SELECTED, DEFAULT_HIGHLIGHT_SELECTED);
    }

}

