package on.browser.pro.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.xdevs23.debugUtils.StackTraceParser;

import on.browser.pro.R;
import on.browser.pro.Unit.BrowserUnit;
import on.browser.pro.Unit.ViewUnit;
import on.browser.pro.View.OnBrowserProToast;
import on.browser.pro.View.OnBrowserProWebView;

public class ScreenshotTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ProgressDialog dialog;
    private OnBrowserProWebView webView;
    private int windowWidth;
    private float contentHeight;
    private String title, path;

    public ScreenshotTask(Context context, OnBrowserProWebView webView) {
        this.context = context;
        this.dialog = null;
        this.webView = webView;
        this.windowWidth = 0;
        this.contentHeight = 0f;
        this.title = null;
        this.path = null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.toast_wait_a_minute));
        dialog.show();

        windowWidth = ViewUnit.getWindowWidth(context);
        contentHeight = webView.getContentHeight() * ViewUnit.getDensity(context);
        title = webView.getTitle();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Bitmap bitmap = ViewUnit.capture(webView, windowWidth, contentHeight, false, Bitmap.Config.ARGB_8888);
            path = BrowserUnit.screenshot(context, bitmap, title);
        } catch (Exception e) {
            path = null;
            StackTraceParser.logStackTrace(e);
        }
        return (path != null && !path.isEmpty());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result)
             OnBrowserProToast.show(context, context.getString(R.string.toast_screenshot_successful) + path);
        else OnBrowserProToast.show(context, R.string.toast_screenshot_failed);

    }

}
