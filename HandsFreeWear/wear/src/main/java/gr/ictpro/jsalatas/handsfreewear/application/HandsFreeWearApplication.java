package gr.ictpro.jsalatas.handsfreewear.application;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.SensorManager;
import android.os.Vibrator;

import java.util.Locale;

/**
 * Created by john on 1/1/18.
 */

public class HandsFreeWearApplication extends Application {
    private static Context context;
    private static Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        HandsFreeWearApplication.context = this.getApplicationContext();
        HandsFreeWearApplication.settings = new Settings();
    }

    public static Context getContext() {
        return context;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static AssetManager getAssetManager() {
        return context.getAssets();
    }

    public static SensorManager getSensorManager() {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public static Vibrator getVibrator() {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static Locale getCurrentLocale() {
        return context.getResources().getConfiguration().getLocales().get(0);
    }
}