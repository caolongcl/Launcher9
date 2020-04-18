package cl.restart.negativescreen.utils;

import android.util.Log;

public class LogUtils {
    private static final String TAG = "Launcher9";

    public static void d(String subTag, String log) {
        Log.d(TAG, subTag + " >>> " + log);
    }

    public static void e(String subTag, String log) {
        Log.e(TAG, subTag + " >>> " + log);
    }
}
