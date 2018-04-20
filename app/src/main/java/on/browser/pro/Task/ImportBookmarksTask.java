package on.browser.pro.Task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import on.browser.pro.Fragment.SettingFragment;
import on.browser.pro.R;
import on.browser.pro.Unit.BrowserUnit;
import on.browser.pro.View.UltimateBrowserProjectToast;

public class ImportBookmarksTask extends AsyncTask<Void, Void, Boolean> {

    private SettingFragment fragment;
    private Context context;
    private ProgressDialog dialog;
    private File file;
    private int count;

    public ImportBookmarksTask(SettingFragment fragment, File file) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.dialog = null;
        this.file = file;
        this.count = 0;
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
        count = BrowserUnit.importBookmarks(context, file);

        if (isCancelled())
            return false;

        return count >= 0;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.hide();
        dialog.dismiss();

        if (result) {
            fragment.setDBChange(true);
            UltimateBrowserProjectToast.show(context, context.getString(R.string.toast_import_bookmarks_successful) + count);
        } else
            UltimateBrowserProjectToast.show(context, R.string.toast_import_bookmarks_failed);
    }

}
