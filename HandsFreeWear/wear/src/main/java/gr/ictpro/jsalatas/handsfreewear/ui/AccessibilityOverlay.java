package gr.ictpro.jsalatas.handsfreewear.ui;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.FrameLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;
import gr.ictpro.jsalatas.handsfreewear.application.Settings;
import gr.ictpro.jsalatas.handsfreewear.infer.Queue;
import gr.ictpro.jsalatas.handsfreewear.service.HandsFreeWearService;

/**
 * Created by john on 1/6/18.
 */

public class AccessibilityOverlay {
    private static AccessibilityOverlay accessibilityOverlay;

    private HandsFreeWearService accessibilityService;

    private final Settings settings = HandsFreeWearApplication.getSettings();

    private final List<AccessibilityNodeInfo> elements = new ArrayList<>();

    private FrameLayout layout;

    private int selectedIndex = -1;

    public static AccessibilityOverlay getInstance() {
        if (accessibilityOverlay == null) {
            accessibilityOverlay = new AccessibilityOverlay();
        }

        return accessibilityOverlay;
    }

    public void setAccessibilityService(HandsFreeWearService accessibilityService) {
        this.accessibilityService = accessibilityService;
    }

    private AccessibilityOverlay() {
    }

    private List<AccessibilityAction> getActionsForGesture(int gesture) {
        List<AccessibilityAction> actions = new ArrayList<>();

        switch (gesture) {
            case 1:
                actions.add(AccessibilityAction.ACTION_SCROLL_LEFT);
                actions.add(AccessibilityAction.ACTION_DISMISS);
                break;
            case 2:
                actions.add(AccessibilityAction.ACTION_SCROLL_RIGHT);
                actions.add(AccessibilityAction.ACTION_DISMISS);
                break;
            case 3:
                actions.add(AccessibilityAction.ACTION_SCROLL_UP);
                actions.add(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                break;
            case 4:
                actions.add(AccessibilityAction.ACTION_SCROLL_DOWN);
                actions.add(AccessibilityAction.ACTION_SCROLL_FORWARD);
                break;
            case 5:
                actions.add(AccessibilityAction.ACTION_CLICK);
                actions.add(AccessibilityAction.ACTION_COLLAPSE);
                actions.add(AccessibilityAction.ACTION_EXPAND);
                break;
            case 6:
            case 14:
                // nothing
                break;
            case 9:
                actions.add(AccessibilityAction.ACTION_SCROLL_LEFT);
                break;
            case 10:
                actions.add(AccessibilityAction.ACTION_SCROLL_RIGHT);
                break;
            case 11:
                actions.add(AccessibilityAction.ACTION_SCROLL_UP);
                actions.add(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                break;
            case 12:
                actions.add(AccessibilityAction.ACTION_SCROLL_DOWN);
                actions.add(AccessibilityAction.ACTION_SCROLL_FORWARD);
                break;
            case 7:
            case 8:
                actions.add(AccessibilityAction.ACTION_SCROLL_DOWN);
                actions.add(AccessibilityAction.ACTION_SCROLL_FORWARD);
                actions.add(AccessibilityAction.ACTION_SCROLL_UP);
                actions.add(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                actions.add(AccessibilityAction.ACTION_SCROLL_RIGHT);
                actions.add(AccessibilityAction.ACTION_DISMISS);
                actions.add(AccessibilityAction.ACTION_CLICK);
                actions.add(AccessibilityAction.ACTION_COLLAPSE);
                actions.add(AccessibilityAction.ACTION_EXPAND);
                actions.add(AccessibilityAction.ACTION_LONG_CLICK);
                break;
            case 13:
                actions.add(AccessibilityAction.ACTION_LONG_CLICK);
                break;

        }


        return actions;
    }

    public void setGesture(int gesture) {
        if (gesture == 0) {
            return;
        }

        Queue.getInstance().disable();

        System.out.println("GESTURE: " + gesture);


        AccessibilityNodeInfo node;
        boolean actionResult;
        if (gesture == 6) {
            actionResult = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        } else if (gesture == 14) {
            actionResult = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
        } else if (gesture == 7 || gesture == 8) {
            List<AccessibilityAction> actions = getActionsForGesture(gesture);
            node = findNode(gesture == 7, actions);
            if (node != null) {
                setSelected(node);
            } else {
                // TODO: do I need this?
                // just blink again the current selection
                //setSelected(getSelectedNode());
            }
            actionResult = true;
        } else if (gesture == 9 || gesture == 10 || gesture == 11 || gesture == 12) {
            int oldSelected = selectedIndex;
            selectedIndex = 0;
            actionResult = swipe(findNode(true, getActionsForGesture(gesture)), gesture, true);
            selectedIndex = oldSelected;
            if (!actionResult && !elements.isEmpty()) {
                // This is speculative: try the swipe gesture in root window
                actionResult = swipe(elements.get(0), gesture, true);
            }
        } else {
            List<AccessibilityAction> actions = getActionsForGesture(gesture);
            actionResult = performAction(actions);
            if (!actionResult && gesture != 5) {
                // TODO: this is speculative over the user's intentions
                // maybe we should have an option
                node = findNode(true, actions);
                actionResult = performAction(node, actions);
            }
            // One more
            if (!actionResult && (gesture == 1 || gesture == 2 || gesture == 3 || gesture == 4)) {
                int oldSelected = selectedIndex;
                selectedIndex = 0;
                actionResult = swipe(findNode(true, getActionsForGesture(gesture)), gesture, false);
                selectedIndex = oldSelected;
                if (!actionResult && !elements.isEmpty()) {
                    // This is speculative: try the swipe gesture in root window
                    actionResult = swipe(elements.get(0), gesture, false);
                }

            }
        }


        if (settings.getVibrate()) {
            HandsFreeWearApplication.getVibrator().vibrate(actionResult ? 100 : 300);
        }

        Queue.getInstance().enable();
    }

    private boolean swipe(AccessibilityNodeInfo node, int gesture, boolean fullScreen) {
        return gesture == 11 || gesture == 12 || gesture == 3 || gesture == 4 ? swipeVertical(node, gesture, fullScreen) : swipeHorizontal(node, gesture, fullScreen);
    }

    private boolean swipeHorizontal(AccessibilityNodeInfo node, int gesture, boolean fullScreen) {
        if (node == null) {
            return false;
        }
        Rect r = new Rect();
        node.getBoundsInScreen(r);
        int vertical = (r.bottom - r.top) / 2;
        int quarter = r.height() / 4;

        int start = gesture == 9 || gesture == 1 ? r.right - (fullScreen ? 1 : quarter) : r.left + (fullScreen ? 1 : quarter);
        int end = gesture == 9 || gesture == 1 ? r.left + (fullScreen ? 1 : quarter) : r.right - (fullScreen ? 1 : quarter);

        GestureDescription.Builder swipe = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(start, vertical);
        p.lineTo(end, vertical);
        swipe.addStroke(new GestureDescription.StrokeDescription(p, 0, 400));
        System.out.println("gesture: " + gesture + " on node: " + node);
        return accessibilityService.dispatchGesture(swipe.build(), null, null);
    }


    private boolean swipeVertical(AccessibilityNodeInfo node, int gesture, boolean fullScreen) {
        if (node == null) {
            return false;
        }
        Rect r = new Rect();
        node.getBoundsInScreen(r);
        int horizontal = (r.right - r.left) / 2;
        int quarter = r.width() / 4;
        int start = gesture == 11 || gesture == 3 ? r.top + (fullScreen ? 1 : quarter) : r.bottom - (fullScreen ? 1 : quarter);
        int end = gesture == 11 || gesture == 3 ? r.bottom - (fullScreen ? 1 : quarter) : r.top + (fullScreen ? 1 : quarter);
        GestureDescription.Builder swipe = new GestureDescription.Builder();
        Path p = new Path();

        p.moveTo(horizontal, start);
        p.lineTo(horizontal, end);
        swipe.addStroke(new GestureDescription.StrokeDescription(p, 0, 400));
        System.out.println("gesture: " + gesture + " on node: " + node);
        return accessibilityService.dispatchGesture(swipe.build(), null, null);
    }


    private boolean performAction(AccessibilityNodeInfo node, List<AccessibilityAction> actions) {
        if (node == null) {
            return false;
        }

        for (AccessibilityAction action : actions) {
            if (containsAction(node, action)) {
                boolean res = node.performAction(action.getId());
                System.out.println("performed Action: " + action.toString() + " on " + node);
                if (res) {
                    System.out.println("   action succeeded");
                    return true;
                }
                System.out.println("   action failed");
            }
        }
        return false;

    }

    private boolean performAction(List<AccessibilityAction> actions) {
        return performAction(getSelectedNode(), actions) || performAction(findNode(true, actions), actions);
    }

    private AccessibilityNodeInfo getParent() {
        AccessibilityNodeInfo selected = getSelectedNode();
        if (selected == null) {
            return null;
        }

        selected = selected.getParent();
        while (selected != null) {
            if (containsAction(selected, AccessibilityAction.ACTION_FOCUS)) {
                return selected;
            }
            selected = selected.getParent();
        }

        return null;
    }

    private AccessibilityNodeInfo findNode(boolean lookForward, List<AccessibilityAction> actions) {
        if (elements.isEmpty()) {
            return null;
        }

        int step = 1;
        int start = lookForward ?
                (selectedIndex != -1 && selectedIndex < elements.size() - 1 ? selectedIndex + 1 : 0) :
                (selectedIndex > 0 ? selectedIndex - 1 : elements.size() - 1);

        int end = elements.size() - 1;

        if (!lookForward) {
            step = -1;
            end = 0;
        }
        for (int i = start; i != end; i += step) {
            AccessibilityNodeInfo node = null;
            try {
                node = elements.get(i);
            } catch (Exception e) {
                // do nothing
            }
            if (node != null && node.isVisibleToUser() && containsAction(node, actions)) {
                return node;
            }
        }

        // Try to wrap around
        if (selectedIndex != -1) {
            if (lookForward) {
                start = 0;
                end = selectedIndex;
            } else {
                start = elements.size() - 1;
                end = selectedIndex;
            }

            for (int i = start; i != end; i += step) {
                AccessibilityNodeInfo node = null;
                try {
                    node = elements.get(i);
                } catch (Exception e) {
                    // do nothing
                }
                if (node != null && node.isVisibleToUser() && containsAction(node, actions)) {
                    return node;
                }
            }
        }
        System.out.println("  not found");
        return null;
    }

    private boolean containsAction(AccessibilityNodeInfo node, AccessibilityAction action) {
        List<AccessibilityAction> actions = new ArrayList<>();
        actions.add(action);
        return containsAction(node, actions);

    }

    private boolean containsAction(AccessibilityNodeInfo node, List<AccessibilityAction> actions) {
        if (node == null) {
            return false;
        }
        List<AccessibilityAction> nodeActions = node.getActionList();
        for (AccessibilityAction na : nodeActions) {
            for (AccessibilityAction a : actions)
                if (na.getId() == a.getId()) {
                    return true;
                }
        }
        return false;
    }


    private void hideOverlay() {
        if (layout != null) {
            WindowManager windowManager = (WindowManager) HandsFreeWearApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
            try {
                assert windowManager != null;
                windowManager.removeView(layout);
            } catch (Exception e) {
                //do nothing
            }
            layout = null;
        }
    }

    private void showOverlay(AccessibilityNodeInfo selected) {
        if (!settings.getHighlightSelected() || selected == null) {
            hideOverlay();
            return;
        }

        int width = 0;
        int height = 0;
        int xpos = 0;
        int ypos = 0;
        int format = PixelFormat.TRANSLUCENT;

        Rect r = new Rect();
        selected.getBoundsInScreen(r);

        xpos = r.left;
        ypos = r.top;
        width = r.width();
        height = r.height();
        format = PixelFormat.OPAQUE;
        System.out.println("Blink: " + selected);

        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams(
                width, height,
                xpos, ypos,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                format);

        overlayParams.gravity = Gravity.START | Gravity.TOP;

        overlayParams.alpha = 100;

        hideOverlay();


        layout = new FrameLayout(HandsFreeWearApplication.getContext());
        layout.setLayoutParams(overlayParams);
        layout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        layout.setBackgroundColor(Color.WHITE);

        WindowManager windowManager = (WindowManager) HandsFreeWearApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        try {
            assert windowManager != null;
            windowManager.addView(layout, overlayParams);
        } catch (Exception e) {
            // do nothing
        }
    }

    public void setRootNode(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            elements.clear();
            selectedIndex = -1;
            return;
        }

        AccessibilityNodeInfo oldSelected = getSelectedNode();
        if (rootNode.getWindow() != null) {
            synchronized (elements) {
                traverseTreeBreadthFirst(rootNode.getWindow().getRoot());
            }
            // TODO: do I need this?
            selectedIndex = 0;
            AccessibilityNodeInfo node = findNode(true, getActionsForGesture(7));
            if (!Objects.equals(oldSelected, node)) {
                setSelected(node);
            } else {
                selectedIndex = elements.indexOf(node);
            }
            System.out.println("--------------------------------------------");
        } else {
            elements.clear();
            selectedIndex = -1;
        }
    }

    private void traverseTreeBreadthFirst(AccessibilityNodeInfo root) {
        elements.clear();
        selectedIndex = -1;

        System.out.println(root);
        elements.add(root);
        for (int i = 0; i < elements.size(); i++) {
            AccessibilityNodeInfo node = elements.get(i);

            if (node != null) {
                for (int j = 0; j < node.getChildCount(); j++) {
                    AccessibilityNodeInfo child = node.getChild(j);
                    if (child != null && !elements.contains(child) && child.isImportantForAccessibility()) {
                        System.out.println(child);
                        elements.add(child);
                    }
                }
            }
        }
    }

    private AccessibilityNodeInfo getSelectedNode() {
        if (selectedIndex == -1 || selectedIndex >= elements.size()) {
            return null;
        }
        return elements.get(selectedIndex);
    }

    private void setSelected(AccessibilityNodeInfo selected) {
        if (selected == null || !selected.isVisibleToUser()) {
            selectedIndex = -1;
            return;
        }
        selectedIndex = elements.indexOf(selected);
        if (settings.getHighlightSelected()) {
            showOverlay(selected);
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOverlay(null);
                }
            }, 250);
        }
    }
}
