package on.browser.pro.Service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import on.browser.pro.Activity.ClearActivity;

public class ClearService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        System.exit(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clear();
        stopSelf();
        return START_STICKY;
    }

    private void clear() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        ClearActivity.clear(sp, this.getApplicationContext());
    }

}
