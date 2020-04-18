package cl.restart.launcher9.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cl.restart.launcher9.MainActivity;
import cl.restart.launcher9.utils.LogUtils;

public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            LogUtils.d(TAG, "action:" + intent.getAction());

            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.setAction(Intent.ACTION_MAIN);
            newIntent.addCategory(Intent.CATEGORY_DEFAULT);
            newIntent.addCategory(Intent.CATEGORY_HOME);
            newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }
    }
}
