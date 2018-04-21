package on.browser.pro.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import on.browser.pro.R;
import on.browser.pro.Unit.BrowserUnit;
import on.browser.pro.View.OnBrowserProToast;

public class ExportBookmarksTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private ProgressDialog dialog;
    private String path;

    public ExportBookmarksTask(Context context) {
        this.context = context;
        this.dialog = null;
        this.path = null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.toast_wait_a_minute));
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        path = BrowserUnit.exportBookmarks(context);

        if (isCancelled())
            return false;

        return path != null && !path.isEmpty();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result)
            OnBrowserProToast.show(context, context.getString(R.string.toast_export_bookmarks_successful) + path);
        else
            OnBrowserProToast.show(context, R.string.toast_export_bookmarks_failed);

    }

}
