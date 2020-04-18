package cl.restart.launcher9;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cl.restart.launcher9.utils.LogUtils;

public class LauncherApp extends Application {
    private static final String TAG = "LauncherApp";

    public static volatile LauncherApp app;
    public static SharedPreferences settings;

    // 创建后台任务executor
    private static final int maxThreads = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            Math.min(2, maxThreads), maxThreads, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), r -> {
        final Thread thread = new Thread(r);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });
    private static final Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }

    public static Context getAppContext() {
        return app;
    }

    public static Resources getAppResources() {
        return app.getResources();
    }

    public static SharedPreferences getSettings() {
        return settings;
    }

    // 管理任务执行
    public static void runBackground(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) runnable.run();
        else threadPool.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) runnable.run();
        else handler.post(runnable);
    }

    public static boolean removeTask(Runnable runnable) {
        return threadPool.remove(runnable);
    }

    // 监控应用生命周期
    public static boolean isForeground() {
        return activitiesCount > 0;
    }

    private static int activitiesCount = 0;
    private static ActivityLifecycleCallbacks activityCbListener = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            ++activitiesCount;
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--activitiesCount == 0) {
                LogUtils.v(TAG, "application background");
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    // 应用crash处理
    static class CrashHandler implements Thread.UncaughtExceptionHandler {

        private static final String TAG = "CrashHandler";

        private Thread.UncaughtExceptionHandler defaultUEH;

        public CrashHandler() {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);

            // Inject some info about android version and the device, since google can't provide them in the developer console
            StackTraceElement[] trace = ex.getStackTrace();
            StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
            System.arraycopy(trace, 0, trace2, 0, trace.length);
            trace2[trace.length] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
            trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
            trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
            ex.setStackTrace(trace2);

            ex.printStackTrace(printWriter);
            String stacktrace = result.toString();
            printWriter.close();
            LogUtils.e(TAG, stacktrace);

            // Save the log on SD card if available
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                writeLog(stacktrace, LauncherApp.getAppContext().getExternalFilesDir(null).getAbsolutePath() + "/vlc_crash");
                writeLogcat(LauncherApp.getAppContext().getExternalFilesDir(null).getAbsolutePath() + "/vlc_logcat");
            }

            defaultUEH.uncaughtException(thread, ex);
        }

        private void writeLog(String log, String name) {
            CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
            String filename = name + "_" + timestamp + ".log";

            FileOutputStream stream;
            try {
                stream = new FileOutputStream(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            try (OutputStreamWriter output = new OutputStreamWriter(stream);
                 BufferedWriter bw = new BufferedWriter(output)) {
                bw.write(log);
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeLogcat(String name) {
            CharSequence timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis());
            String filename = name + "_" + timestamp + ".log";
            try {
                _writeLogcat(filename);
            } catch (IOException e) {
                LogUtils.e(TAG, "Cannot write logcat to disk");
            }
        }

        private void _writeLogcat(String filename) throws IOException {
            FileOutputStream fileStream;
            try {
                fileStream = new FileOutputStream(filename);
            } catch (FileNotFoundException e) {
                return;
            }

            String[] args = {"logcat", "-v", "time", "-d"};

            Process process = Runtime.getRuntime().exec(args);

            try (InputStreamReader input = new InputStreamReader(process.getInputStream());
                 OutputStreamWriter output = new OutputStreamWriter(fileStream);
                 BufferedReader br = new BufferedReader(input);
                 BufferedWriter bw = new BufferedWriter(output)) {
                String line;
                while ((line = br.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (Exception e) {
            }
        }
    }
}
