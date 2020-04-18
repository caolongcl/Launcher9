package cl.restart.launcher9.model;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

public class PagerInfo {
    private List<AbsAppInfo> mAbsAppInfoList;
    private HashMap<String, AbsAppInfo> mAbsAppInfoMap;
    private int mMaxNum;

    public PagerInfo(@NonNull List<AbsAppInfo> appInfos, int max) {
        mAbsAppInfoList = appInfos;
        mAbsAppInfoMap = mAbsAppInfoList.parallelStream().collect(HashMap::new,
                (hashMap, absAppInfo) -> {
                    if (absAppInfo != null && absAppInfo.isAppInfoType()) {
                        AppInfo appInfo = (AppInfo) absAppInfo;
                        hashMap.put(getKey(appInfo.getAppInfo().activityInfo.packageName,
                                appInfo.getAppInfo().activityInfo.name), appInfo);
                    }
                }, HashMap::putAll);
        mMaxNum = max;
    }

    public void update(@NonNull List<AbsAppInfo> appInfos) {
        mAbsAppInfoList.clear();
        mAbsAppInfoMap.clear();

        mAbsAppInfoList = appInfos;
        mAbsAppInfoMap = mAbsAppInfoList.parallelStream().collect(HashMap::new,
                (hashMap, absAppInfo) -> {
                    if (absAppInfo.isAppInfoType()) {
                        AppInfo appInfo = (AppInfo) absAppInfo;
                        hashMap.put(getKey(appInfo.getAppInfo().activityInfo.packageName,
                                appInfo.getAppInfo().activityInfo.name), appInfo);
                    }
                }, HashMap::putAll);
    }

    public List<AbsAppInfo> getPager() {
        return mAbsAppInfoList;
    }

    public AbsAppInfo get(int index) {
        if (index >= 0 && index < mAbsAppInfoList.size()) {
            return mAbsAppInfoList.get(index);
        } else {
            return null;
        }
    }

    public AbsAppInfo get(String packageName, String activityName) {
        String key = getKey(packageName, activityName);
        return mAbsAppInfoMap.get(key);
    }

    public boolean add(AbsAppInfo absAppInfo) {
        boolean ret = false;
        if (absAppInfo != null && !isFull()) {
            ret = mAbsAppInfoList.add(absAppInfo);
            if (absAppInfo.isAppInfoType()) {
                AppInfo appInfo = (AppInfo) absAppInfo;
                mAbsAppInfoMap.put(getKey(appInfo.getAppInfo().activityInfo.packageName,
                        appInfo.getAppInfo().activityInfo.name), absAppInfo);
            }
        }

        return ret;
    }

    public boolean add(List<AbsAppInfo> list) {
        boolean ret = false;
        if (list != null && list.size() != 0 && (mAbsAppInfoList.size() + list.size()) <= mMaxNum) {
            if (list.stream().anyMatch(AbsAppInfo::isAppFolderInfoType)) {
                return false;
            }

            list.forEach(absAppInfo -> add(absAppInfo));
            ret = true;
        }

        return ret;
    }

    public boolean remove(AbsAppInfo absAppInfo) {
        boolean ret = false;
        if (absAppInfo != null && !isEmpty()) {
            ret = mAbsAppInfoList.remove(absAppInfo);
            if (absAppInfo.isAppInfoType()) {
                AppInfo appInfo = (AppInfo) absAppInfo;
                mAbsAppInfoMap.remove(getKey(appInfo.getAppInfo().activityInfo.packageName,
                        appInfo.getAppInfo().activityInfo.name));
            }
        }

        return ret;
    }

    public boolean swap(int i, int j) {
        if (validIndex(i) && validIndex(j)) {
            AbsAppInfo absAppInfoi = mAbsAppInfoList.get(i);
            AbsAppInfo absAppInfoj = mAbsAppInfoList.get(j);
            int idi = absAppInfoi.getId();
            int idj = absAppInfoj.getId();

            absAppInfoi.setId(idj);
            absAppInfoj.setId(idi);

            mAbsAppInfoList.set(j, absAppInfoi);
            mAbsAppInfoList.set(i, absAppInfoj);

            return true;
        }

        return false;
    }

    public boolean isFull() {
        return mAbsAppInfoList.size() == mMaxNum;
    }

    public boolean isEmpty() {
        return mAbsAppInfoList.size() == 0;
    }

    public int size() {
        return mAbsAppInfoList.size();
    }

    private String getKey(String packageName, String activityName) {
        return packageName + activityName;
    }

    private boolean validIndex(int i) {
        return i >= 0 && i < mAbsAppInfoList.size();
    }
}
