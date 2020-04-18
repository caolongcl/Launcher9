package cl.restart.launcher9.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import cl.restart.launcher9.AppHelper;
import cl.restart.launcher9.dao.SDao;
import cl.restart.launcher9.utils.LogUtils;

public class AppInfoLoader {
    private static final String TAG = "AppInfoLoader";
    private Context mContext;
    private List<ResolveInfo> mApps;

    private List<PagerInfo> mPagerList = new ArrayList<>();

    private PagerInfo mShortcutBarApps;
    private SDao mDao;

    public AppInfoLoader(Context context) {
        mContext = context;
        mDao = ((AppHelper) context).getDao();
    }

    public void loadApps() {
        loadAppsFromSystem();
        loadPagers();
    }

    private void loadAppsFromSystem() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    private void loadPagers() {
//        List<AppInDB> appInDBS = mDao.loadAppInDB();
        List<AppInDB> appInDBS = null;

        mShortcutBarApps = new PagerInfo(new ArrayList<>(), mDao.getShortcutBarColumns());

        if (appInDBS == null) {
            List<AbsAppInfo> shortcutInfos = new ArrayList<>();
            for (int i = 0; i < mDao.getShortcutBarColumns(); i++) {
                shortcutInfos.add(new AppInfo(null));
            }

            final int onePagerAppNum = mDao.getDesktopPagerColumns() * mDao.getDesktopPagerRows();
            final int appNum = mApps.size() - 1;
            int pageNum = appNum / onePagerAppNum;
            int tmp = appNum % onePagerAppNum;
            if (tmp > 0) {
                pageNum += 1;
            }
            LogUtils.d(TAG, "app num:" + appNum + ",pager num:" + pageNum);

            int k = 0;
            List<AbsAppInfo> infos = null;
            for (int i = 0, j = 0; i < mApps.size(); i++) {
                if (mContext.getPackageName().equals(mApps.get(i).activityInfo.packageName)) {
                    continue;
                }

                AppInfo info = new AppInfo(mApps.get(i));

                if (j % onePagerAppNum == 0) {
                    if (infos != null) {
                        mPagerList.add(new PagerInfo(infos, onePagerAppNum));
                    }
                    infos = new ArrayList<>();
                    k = 0;
                }

                j++;
                k++;
                info.setId(k);

                if ("com.android.contacts".equals(info.getAppInfo().activityInfo.packageName)
                        && "com.android.contacts.activities.DialtactsActivity".equals(info.getAppInfo().activityInfo.name)) {
                    shortcutInfos.set(0, new AppInfo(mApps.get(i)));
                } else if ("com.android.mms".equals(info.getAppInfo().activityInfo.packageName)) {
                    shortcutInfos.set(1, new AppInfo(mApps.get(i)));
                } else if ("com.hmdglobal.camera2".equals(info.getAppInfo().activityInfo.packageName)) {
                    shortcutInfos.set(2, new AppInfo(mApps.get(i)));
                } else if ("com.android.chrome".equals(info.getAppInfo().activityInfo.packageName)) {
                    shortcutInfos.set(3, new AppInfo(mApps.get(i)));
                } else if ("com.android.settings".equals(info.getAppInfo().activityInfo.packageName)) {
                    shortcutInfos.set(4, new AppInfo(mApps.get(i)));
                }

                infos.add(info);
            }
            mPagerList.add(new PagerInfo(infos, onePagerAppNum));

            int i = 0;
            do {
                AppInfo appInfo = (AppInfo) shortcutInfos.get(i);
                if (!appInfo.valid()) {
                    shortcutInfos.remove(appInfo);
                } else {
                    appInfo.setId(i);
                    ++i;
                }
            } while (i < shortcutInfos.size());

            mShortcutBarApps.update(shortcutInfos);

//            mDao.insert(mPagerList, mShortcutBarApps);
        } else {

        }
    }

    public int getPagerNum() {
        return mPagerList.size();
    }

    public PagerInfo getPagerInfo(int index) {
        if (index >= mPagerList.size()) {
            LogUtils.e(TAG, "out of index " + index + ",max " + (mPagerList.size() - 1));
            return null;
        } else {
            return mPagerList.get(index);
        }
    }

    public PagerInfo getShortcutPagerInfo() {
        return mShortcutBarApps;
    }
}
