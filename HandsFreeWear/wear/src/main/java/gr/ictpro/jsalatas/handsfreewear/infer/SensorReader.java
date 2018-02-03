package gr.ictpro.jsalatas.handsfreewear.infer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;

/**
 * Created by john on 1/6/18.
 */

public class SensorReader implements SensorEventListener {
    private static SensorReader sensorReader;

    private final SensorManager mSensorManager;
    private final Sensor mAccelerometerSensor;
    private final Queue queue = Queue.getInstance();


    public static SensorReader getInstance() {
        if(sensorReader == null) {
            sensorReader = new SensorReader();
        }

        return sensorReader;
    }

    private SensorReader() {
        mSensorManager = HandsFreeWearApplication.getSensorManager();
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    void enable() {
        Queue.getInstance().reset();

        registerSensor();
    }

    void disable() {
        unRegisterSensor();
    }


    private void registerSensor() {
        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }


    private void unRegisterSensor() {
        mSensorManager.unregisterListener(this, mAccelerometerSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float[] values = new float[3];
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ) {
            values[0] = (int)(event.values[0] * 100.);
            values[1] = (int)(event.values[1] * 100.);
            values[2] = (int)(event.values[2] * 100.);

//            handler.post(new Runnable() {
//                public void run() {
//                    queue.add(values);
//                }
//            });
            queue.add(values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: should I consider this?
    }

}
