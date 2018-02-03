package gr.ictpro.jsalatas.handsfreewear.service;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;
import java.util.Objects;

import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;
import gr.ictpro.jsalatas.handsfreewear.infer.Queue;
import gr.ictpro.jsalatas.handsfreewear.ui.AccessibilityOverlay;
import gr.ictpro.jsalatas.handsfreewear.ui.GetPermissionsActivity;

/**
 * Created by john on 11/28/17.
 */

public class HandsFreeWearService extends AccessibilityService {

    private long lastEvent = 0;

    public class ScreenOnOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)) {
                Queue.getInstance().enable();
                lastEvent = 0;
            } else if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                Queue.getInstance().disable();
            }
        }
    }

    private ScreenOnOffReceiver mScreenOnOffReceiver;


    @Override
    public void onCreate() {
        super.onCreate();

        // Register ScreenOnOff Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOnOffReceiver = new ScreenOnOffReceiver();
        registerReceiver(mScreenOnOffReceiver, filter);
    }

    @Override
    protected void onServiceConnected() {
        Intent overlayPermissionIntent = new Intent(this, GetPermissionsActivity.class);
        startActivity(overlayPermissionIntent);

        AccessibilityOverlay.getInstance().setAccessibilityService(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mScreenOnOffReceiver);

        Queue.getInstance().disable();

        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(Queue.getInstance().isDisabled()) {
            return;
        }
        System.out.println("EVENT: " + event);

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            List<AccessibilityWindowInfo> windows = getWindows();
            if(windows != null && windows.size()>0) {
                AccessibilityOverlay.getInstance().setRootNode(getWindows().get(0).getRoot());
            }
        }

    }

    @Override
    public void onInterrupt() {

    }

}
