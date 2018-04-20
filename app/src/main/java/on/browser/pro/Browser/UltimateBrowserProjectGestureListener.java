package on.browser.pro.Browser;

import android.view.GestureDetector;
import android.view.MotionEvent;

import on.browser.pro.View.UltimateBrowserProjectWebView;

public class UltimateBrowserProjectGestureListener extends GestureDetector.SimpleOnGestureListener {

    private UltimateBrowserProjectWebView webView;
    private boolean longPress = true;

    public UltimateBrowserProjectGestureListener(UltimateBrowserProjectWebView webView) {
        super();
        this.webView = webView;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (longPress) webView.onLongPress();
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        longPress = false;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        longPress = true;
    }

}
