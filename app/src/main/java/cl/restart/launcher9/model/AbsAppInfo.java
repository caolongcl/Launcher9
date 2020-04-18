package cl.restart.launcher9.model;

public abstract class AbsAppInfo {
    public static final int APP_TYPE = 0;
    public static final int APP_FOLDER_TYPE = 1;

    protected int mType;
    protected boolean mVisible;
    protected int mId;

    public int getType() {
        return mType;
    }

    public boolean isAppInfoType() {
        return mType == APP_TYPE;
    }

    public boolean isAppFolderInfoType() {
        return mType == APP_FOLDER_TYPE;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }
}
