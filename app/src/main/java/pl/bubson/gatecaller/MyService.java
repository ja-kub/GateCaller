package pl.bubson.gatecaller;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Kuba on 2016-10-18.
 */
public class MyService extends Service {

    private static final String TAG = "MyService";
    private final CallReceiver myBroadcast = CallReceiver.getInstance();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // The service is being created
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(myBroadcast, filter);
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        try {
            unregisterReceiver(myBroadcast);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
