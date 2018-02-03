package gr.ictpro.jsalatas.sensordata;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.GregorianCalendar;

public class SensorData extends AccessibilityService implements SensorEventListener {
    private static final String SERVICE_TYPE = "_http._tcp";
    public static final String SERVICE_NAME = "GestureRecorder";

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private String serviceHost;
    private Integer servicePort;
    private boolean destroying;
    private boolean servicefound;


    public SensorData() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        destroying = false;
        initializeDiscoveryListener();
        initializeResolveListener();


        startDiscoverTask();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this, mAccelerometerSensor);
        destroying = true;
        stopDiscoverTask();
    }

    void startDiscoverTask() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!servicefound && !destroying) {
                    stopDiscoverTask();
                }
            }
        }, 5000);
    }

    void stopDiscoverTask() {
        mSensorManager.unregisterListener(SensorData.this, mAccelerometerSensor);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int[] values = new int[3];
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            values[0] = (int) (event.values[0] * 100.);
            values[1] = (int) (event.values[1] * 100.);
            values[2] = (int) (event.values[2] * 100.);
        }
        final long time = GregorianCalendar.getInstance().getTimeInMillis();

        if(servicefound) {
            new SendDataTask().execute("http://" + serviceHost + ":" + servicePort + "/?time=" + time + "&x=" + values[0] + "&y=" + values[1] + "&z=" + values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO: should I consider this?
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {}


            @Override
            public void onServiceFound(NsdServiceInfo service) {
                if (service.getServiceType().contains(SERVICE_TYPE) && service.getServiceName().equals(SERVICE_NAME)) {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                if (service.getServiceType().contains(SERVICE_TYPE) && service.getServiceName().equals(SERVICE_NAME)) {
                    Log.d("DISCOVER", "Service Lost");
                    servicefound = false;
                    mSensorManager.unregisterListener(SensorData.this, mAccelerometerSensor);
                    // try finding it again
                    stopDiscoverTask();
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                servicefound = false;
                mSensorManager.unregisterListener(SensorData.this, mAccelerometerSensor);
                if (!destroying) {
                    startDiscoverTask();
                }
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {}

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {}
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {}

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                if (serviceInfo.getServiceType().contains(SERVICE_TYPE) && serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                    Log.d("DISCOVER", "Service Found");
                    servicefound = true;
                    NsdServiceInfo mService = serviceInfo;
                    servicePort = mService.getPort();
                    serviceHost = mService.getHost().getHostAddress();
                    mSensorManager.registerListener(SensorData.this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
                }
            }
        };
    }
}

