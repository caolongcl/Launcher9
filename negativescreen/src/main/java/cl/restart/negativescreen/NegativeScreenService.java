package cl.restart.negativescreen;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import cl.restart.negativescreen.model.ParamsWrap;
import cl.restart.negativescreen.utils.LogUtils;
import cl.restart.negativescreen.view.MainOverlay;

public class NegativeScreenService extends Service {
    private static final String TAG = "NegativeScreenService";

    private ParamsWrap mParamsWrap;
    private ILauncherOverlayCallback mILauncherOverlayCallback;
    private MainOverlay mMainOverlay;
    private int mWindowShift;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public NegativeScreenService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate");

        Log.e(TAG, "initNotification");
        NotificationChannel channel = new NotificationChannel(TAG, TAG,
                NotificationManager.IMPORTANCE_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), TAG).build();
            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        LogUtils.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "onBind");
        return new MyBinder();
    }

    private void createOverlay(Context context) {
        mMainOverlay = new MainOverlay(context);
        mWindowShift = 0;

        try {
            mILauncherOverlayCallback.overlayStatusChanged(1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class MyBinder extends ILauncherOverlay.Stub {
        public void onResume() throws RemoteException {
            LogUtils.d(TAG, "onResume");
        }

        public void onPause() throws RemoteException {
            LogUtils.d(TAG, "onPause");
        }

        public void windowAttached(ParamsWrap attrs, ILauncherOverlayCallback callbacks, int options) throws RemoteException {
            LogUtils.d(TAG, "windowAttached");
            mParamsWrap = attrs;
            mILauncherOverlayCallback = callbacks;

            mHandler.post(()->{
                createOverlay(NegativeScreenService.this);
            });
        }

        public void windowDetached(boolean isChangingConfigurations) throws RemoteException {
            LogUtils.d(TAG, "windowDetached");
        }

        public void openOverlay(int options) throws RemoteException {
            LogUtils.d(TAG, "openOverlay");
            mHandler.post(()->{
                if (mMainOverlay != null){
                    mMainOverlay.show();
                }
            });
        }

        public void closeOverlay(int options) throws RemoteException {
            LogUtils.d(TAG, "closeOverlay");
            mHandler.post(()->{
                if (mMainOverlay != null){
                    mMainOverlay.dismiss();
                }
            });
        }

        public void startScroll() throws RemoteException {
            LogUtils.d(TAG, "startScroll");
        }

        public void onScroll(float progress) throws RemoteException {
            LogUtils.d(TAG, "onScroll " + progress);
            if (mILauncherOverlayCallback != null){
                mILauncherOverlayCallback.overlayScrollChanged(progress);
            }
        }

        public void endScroll() throws RemoteException {
            LogUtils.d(TAG, "endScroll");
        }

        public void requestVoiceDetection(boolean start) throws RemoteException {
            LogUtils.d(TAG, "requestVoiceDetection");
        }

        public String getVoiceSearchLanguage() throws RemoteException {
            LogUtils.d(TAG, "getVoiceSearchLanguage");
            return "";
        }

        public boolean isVoiceDetectionRunning() throws RemoteException {
            LogUtils.d(TAG, "isVoiceDetectionRunning");
            return false;
        }
    }
}
