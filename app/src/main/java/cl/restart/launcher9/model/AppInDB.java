package cl.restart.launcher9.model;

import android.support.annotation.NonNull;

public class AppInDB {
    private String mDesktopId;
    private String mAppId;
    private String mSubAppId;
    private String mPackageName;
    private String mAppName;

    public AppInDB() {

    }

    public boolean isShortcutBarApp() {
        return mDesktopId.startsWith(Constants.SHORTCUT_BAR_DESKTOP_ID);
    }

    public boolean isDesktopApp() {
        return mDesktopId.startsWith(Constants.DESKTOP_PAGER_ID_PREFIX);
    }

    public boolean isNormalApp() {
        return mAppId.startsWith(Constants.DESKTOP_NORMAL_APP_ID_PREFIX);
    }

    public boolean isFolderApp() {
        return mAppId.startsWith(Constants.DESKTOP_FOLDER_APP_ID_PREFIX);
    }

    public boolean isSubApp() {
        return mSubAppId.startsWith(Constants.DESKTOP_SUB_APP_ID_PREFIX);
    }

    public void setDesktopId(String desktopid) {
        mDesktopId = desktopid;
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }

    public void setSubAppId(String subAppId) {
        mSubAppId = subAppId;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getDesktopId() {
        return mDesktopId;
    }

    public String getAppId() {
        return mAppId;
    }

    public String getSubAppId() {
        return mSubAppId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    @NonNull
    @Override
    public String toString() {
        return "APP:{desktop_id:" + mDesktopId +
                ", app_id:" + mAppId +
                ", sub_app_id:" + mSubAppId +
                ", package_name:" + mPackageName +
                ", app_name:" + mAppName +
                "}";
    }
}
