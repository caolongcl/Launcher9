package cl.restart.launcher9;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import cl.restart.launcher9.dao.SDao;
import cl.restart.launcher9.desktopPager.DesktopPager;
import cl.restart.launcher9.model.AppInfoLoader;
import cl.restart.launcher9.model.IInterface;
import cl.restart.launcher9.pagerIndicator.PagerIndicator;
import cl.restart.launcher9.shortcutBar.ShortcutBar;
import cl.restart.launcher9.utils.LogUtils;
import cl.restart.negativescreen.ILauncherOverlay;
import cl.restart.negativescreen.ILauncherOverlayCallback;
import cl.restart.negativescreen.model.ParamsWrap;

public class MainActivity extends AppCompatActivity implements AppHelper, IInterface.OnEditDesktopListener,
        ViewGroup.OnTouchListener {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private DesktopPager mDesktopPager;
    private ShortcutBar mShortcutBar;
    private PagerIndicator mPagerIndicatorLL;
    private AppInfoLoader mAppLoader;
    private SDao mDao;
    private ILauncherOverlay mLauncherOverlay;
    private ParamsWrap mParamsWrap = new ParamsWrap();

    @Override
    public void onAttachedToWindow() {
        LogUtils.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(TAG, "onCreate");

        mDao = new SDao(this);
        mAppLoader = new AppInfoLoader(this);
        mAppLoader.loadApps();

        setFullScreen();
        setContentView(R.layout.activity_main);

        mDesktopPager = (DesktopPager) findViewById(R.id.app_list);
        mShortcutBar = (ShortcutBar) findViewById(R.id.app_shortcut_bar);
        mPagerIndicatorLL = (PagerIndicator) findViewById(R.id.app_pager_indicator);

        registerReceiver();
        bindService();

        loadAppPager();
        loadShortcutBar();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        unregisterReceiver();
        mDao.onDestroy();
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
    }

    private void loadShortcutBar() {
        mShortcutBar.init(mAppLoader);
    }

    private void loadAppPager() {
        mPagerIndicatorLL.init(mAppLoader);
        mDesktopPager.init(mAppLoader);
        mDesktopPager.addOnDesktopChangeListener(mOnDesktopChangeListener);
        mDesktopPager.setOnTouchListener(this::onTouch);
    }

    // 设置全屏
    private void setFullScreen() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    // 壁纸更换
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, action);

            if (action == null) {
                return;
            }

            switch (action) {
                case Intent.ACTION_SET_WALLPAPER:
                case Intent.ACTION_WALLPAPER_CHANGED:
                    mShortcutBar.onWallpaperChanged();
                    break;
                case Intent.ACTION_PACKAGE_ADDED:
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    break;
                case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                    String reason = intent.getStringExtra("reason");
                    if (reason != null) {
                        switch (reason) {
                            case "homekey":
                                mShortcutBar.onHomeKeyPressed();
                                mDesktopPager.onHomeKeyPressed();
                                break;
                            case "recentapps":
                                mShortcutBar.onRecentKeyPressed();
                                mDesktopPager.onRecentKeyPressed();
                                break;
                        }
                    }
                    break;
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SET_WALLPAPER);
        filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mReceiver);
    }

    private ServiceConnection mLauncherOverlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected");
            mLauncherOverlay = ILauncherOverlay.Stub.asInterface(service);
            LogUtils.d(TAG, "ILauncherOverlay " + mLauncherOverlay);
            try {
                if (mLauncherOverlay != null) {
                    try {
                        mParamsWrap.setLayoutParams(getWindow().getAttributes());
                        mLauncherOverlay.windowAttached(mParamsWrap, mLauncherOverlayCallback, 0);
                    } catch (RemoteException e) {
                        LogUtils.d(TAG, "ILauncherOverlay " + e.getMessage());
                        e.printStackTrace();
                    }
                    mLauncherOverlay.openOverlay(0);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLauncherOverlay = null;
        }
    };

    private ILauncherOverlayCallback mLauncherOverlayCallback = new ILauncherOverlayCallback.Stub() {
        @Override
        public void overlayScrollChanged(float progress) throws RemoteException {
            LogUtils.d(TAG, "overlayScrollChanged " + progress);
            MainActivity.this.getWindow().getDecorView().setTranslationX(progress);
            float x = progress + mStartX;
//            if (x > )
        }

        @Override
        public void overlayStatusChanged(int status) throws RemoteException {
            LogUtils.d(TAG, "overlayStatusChanged " + status);
        }
    };

    private void bindService() {
        Intent intent = new Intent();
        intent.setAction("cl.restart.negativescreen.NegativeScreenService");
        intent.setPackage("cl.restart.negativescreen");
        //        startForegroundService(intent);
        boolean ret = bindService(intent, mLauncherOverlayServiceConnection, BIND_AUTO_CREATE);
        LogUtils.d(TAG, "service ret:" + ret);
    }

    private void unbindService() {
        unbindService(mLauncherOverlayServiceConnection);
    }

    @Override
    public void onBackPressed() {
        mShortcutBar.onBackPressed();
        mDesktopPager.onBackPressed();
    }

    @Override
    public void startApp(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.app_start, R.anim.app_no_anim);
    }

    @Override
    public void finishApp() {
    }

    @Override
    public SDao getDao() {
        return mDao;
    }

    @Override
    public void editDesktop() {
        mShortcutBar.onEditDesktop();
        mDesktopPager.onEditDesktop();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        LogUtils.d(TAG, "on touch " + mPagerIndicatorLL.isMostLeft());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = (int) event.getX();
                LogUtils.d(TAG, "ACTION_DOWN = " + mStartX);
                break;
            case MotionEvent.ACTION_MOVE:
                // 获取滑动时候的X距离
                mSlipX = (int) event.getX() - mStartX;
//                LogUtils.d(TAG, "ACTION_MOVE= " + mSlipX);
                if (mPagerIndicatorLL.isMostLeft()) {
//                    LogUtils.d(TAG, "ACTION_MOVE most left");
                    if (mLauncherOverlay != null) {
                        try {
                            mLauncherOverlay.onScroll(mSlipX);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mLauncherOverlay != null) {
                    try {
                        mLauncherOverlay.endScroll();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    private int mSlipX = 0;
    private int mStartX = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 一定要spuer，否则事件打住,不会在向下调用了
        super.dispatchTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = (int) ev.getX();
                LogUtils.d(TAG, "StartX = " + mStartX);
                if (mPagerIndicatorLL.isMostLeft()) {
                    if (mLauncherOverlay != null) {
                        try {
                            mLauncherOverlay.startScroll();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
        return true;
    }

    private DesktopPager.OnDesktopChangeListener mOnDesktopChangeListener = new DesktopPager.OnDesktopChangeListener() {
        @Override
        public void onDesktopScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            LogUtils.d(TAG, "onDesktopScrolled pos:"+position +" "+positionOffset +" " +positionOffsetPixels);
        }

        @Override
        public void onDesktopChanged(int index) {
//            LogUtils.d(TAG, "onDesktopChanged:"+index);
            mPagerIndicatorLL.check(index);
        }

        @Override
        public void onDesktopScrollStateChanged(int state) {
//            LogUtils.d(TAG, "onDesktopScrollStateChanged:"+state);
        }
    };
}
