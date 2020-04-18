package cl.restart.launcher9.model;

import android.content.pm.ResolveInfo;

import java.util.List;

public class AppFolderInfo extends AbsAppInfo {
    private List<ResolveInfo> mAppInfoInnerList;

    public AppFolderInfo(List<ResolveInfo> infos) {
        mType = APP_FOLDER_TYPE;
    }

    public void setAppInfo(List<ResolveInfo> infos) {
        mAppInfoInnerList = infos;
    }

    public List<ResolveInfo> getAppInfoList() {
        return mAppInfoInnerList;
    }
}
