package in.jiyawebsolution.jiyaai;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class JiyaAccessibilityService extends AccessibilityService {
    private static JiyaAccessibilityService instance;
    @Override protected void onServiceConnected() { instance = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() { instance = null; super.onDestroy(); }

    public static boolean isRunning() { return instance != null; }
    public static boolean goBack() {
        return instance != null && instance.performGlobalAction(GLOBAL_ACTION_BACK);
    }
    public static boolean goHome() {
        return instance != null && instance.performGlobalAction(GLOBAL_ACTION_HOME);
    }
    public static boolean openRecents() {
        return instance != null && instance.performGlobalAction(GLOBAL_ACTION_RECENTS);
    }
}
