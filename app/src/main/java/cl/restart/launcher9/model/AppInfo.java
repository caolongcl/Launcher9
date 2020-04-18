package cl.restart.launcher9.model;

import android.content.pm.ResolveInfo;

import java.util.List;

public class AppInfo extends AbsAppInfo {
    private ResolveInfo mAppInfoInner;

    public AppInfo(ResolveInfo info) {
        mType = APP_TYPE;
        mAppInfoInner = info;
    }

    public void setAppInfo(ResolveInfo info) {
        mAppInfoInner = info;
    }

    public ResolveInfo getAppInfo() {
        return mAppInfoInner;
    }

    public boolean valid() {
        return mAppInfoInner != null;
    }

    public String getPackageName() {
        return mAppInfoInner.activityInfo.packageName;
    }

    public String getAppName() {
        return mAppInfoInner.activityInfo.name;
    }
}
