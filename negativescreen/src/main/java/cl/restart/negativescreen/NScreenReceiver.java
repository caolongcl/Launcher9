package cl.restart.negativescreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cl.restart.negativescreen.utils.LogUtils;

public class NScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "NScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(TAG, "onReceiver " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            LogUtils.d(TAG, "onReceiver boot complete.");
            Intent intent1 = new Intent(context, NegativeScreenService.class);
            context.startForegroundService(intent1);
        }
    }
}
