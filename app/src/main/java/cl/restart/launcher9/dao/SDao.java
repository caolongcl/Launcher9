package cl.restart.launcher9.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.restart.launcher9.R;
import cl.restart.launcher9.model.AbsAppInfo;
import cl.restart.launcher9.model.AppInDB;
import cl.restart.launcher9.model.AppInfo;
import cl.restart.launcher9.model.Constants;
import cl.restart.launcher9.model.PagerInfo;
import cl.restart.launcher9.utils.LogUtils;

public class SDao {
    private static final String TAG = "SDao";

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private SharedPrefs mSharedPrefs;

    private int mDesktopPagerColumns;
    private int mDesktopPagerRows;
    private int mShortcutBarColumns;
    private int mShortcutBarVisibleColumns;
    private int mDesktopPagerNum;

    public SDao(Context context) {
        mDBHelper = new DBHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        mSharedPrefs = new SharedPrefs(context);
        initSharePrefs();
    }

    public void onDestroy() {
        mDB.close();
    }

    public int getDesktopPagerColumns() {
        return mDesktopPagerColumns;
    }

    public int getDesktopPagerRows() {
        return mDesktopPagerRows;
    }

    public int getShortcutBarColumns() {
        return mShortcutBarColumns;
    }

    public int getShortcutBarVisibleColumns() {
        return mShortcutBarVisibleColumns;
    }

    private void initSharePrefs() {
        mDesktopPagerColumns = (int) mSharedPrefs.get(R.string.sp_desktop_columns, Constants.DEFAULT_DESKTOP_PAGER_COLUMNS);
        mDesktopPagerRows = (int) mSharedPrefs.get(R.string.sp_desktop_rows, Constants.DEFAULT_DESKTOP_PAGER_ROWS);
        mShortcutBarColumns = (int) mSharedPrefs.get(R.string.sp_shortcurbar_columns, Constants.DESKTOP_SHORTCUTBAR_COLUMNS);
        mShortcutBarVisibleColumns = (int) mSharedPrefs.get(R.string.sp_shortcutbar_visible_columns, Constants.DEFAULT_SHORTCUTBAR_VISIBLE_COLUMNS);
        mDesktopPagerNum = (int) mSharedPrefs.get(R.string.sp_desktop_num, 1);
    }

    public List<AppInDB> loadAppInDB() {
        List<AppInDB> appInDBS = null;

        String sql = "select * from " + Table.AppLayout.TABLE_NAME + " order by " + Table.ID + " asc";
        Cursor cursor = mDB.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            appInDBS = new ArrayList<>();
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                AppInDB appInDB = new AppInDB();
                appInDB.setDesktopId(cursor.getString(cursor.getColumnIndex(Table.AppLayout.DESKTOP_ID)));
                appInDB.setAppId(cursor.getString(cursor.getColumnIndex(Table.AppLayout.APP_ID)));
                appInDB.setSubAppId(cursor.getString(cursor.getColumnIndex(Table.AppLayout.SUB_APP_ID)));
                appInDB.setPackageName(cursor.getString(cursor.getColumnIndex(Table.AppLayout.PACKAGE_NAME)));
                appInDB.setAppName(cursor.getString(cursor.getColumnIndex(Table.AppLayout.APP_NAME)));

                appInDBS.add(appInDB);
                LogUtils.d(TAG, appInDB.toString());
            }

        }
        cursor.close();

        return appInDBS;
    }

    public void insert(@NonNull List<PagerInfo> pagerInfos, PagerInfo shortcutbarPagerInfos) {
        mSharedPrefs.clear();
        mSharedPrefs.put(R.string.sp_desktop_num, pagerInfos.size());
        StringBuilder sql;

        mDB.execSQL("delete from " + Table.AppLayout.TABLE_NAME);
        for (int i = 0; i < pagerInfos.size(); i++) {
            PagerInfo pagerInfo = pagerInfos.get(i);
            for (AbsAppInfo absAppInfo : pagerInfo.getPager()) {
                AppInfo appInfo = (AppInfo) absAppInfo;
                sql = new StringBuilder();
                sql.append("insert into " + Table.AppLayout.TABLE_NAME + " (" +
                        Table.AppLayout.DESKTOP_ID + "," +
                        Table.AppLayout.APP_ID + "," +
                        Table.AppLayout.SUB_APP_ID + "," +
                        Table.AppLayout.PACKAGE_NAME + "," +
                        Table.AppLayout.APP_NAME + ") values(");
                sql.append("'").append(Constants.DESKTOP_PAGER_ID_PREFIX).append(i).append("',");
                sql.append("'").append(Constants.DESKTOP_NORMAL_APP_ID_PREFIX).append(appInfo.getId()).append("',");
                sql.append("'").append(Constants.DESKTOP_SUB_APP_ID_PREFIX).append(0).append("',");
                sql.append("'").append(appInfo.getPackageName()).append("',");
                sql.append("'").append(appInfo.getAppName()).append("')");
                mDB.execSQL(sql.toString());
            }
        }

        for (AbsAppInfo absAppInfo : shortcutbarPagerInfos.getPager()) {
            AppInfo appInfo = (AppInfo) absAppInfo;
            sql = new StringBuilder();
            sql.append("insert into " + Table.AppLayout.TABLE_NAME + " (" +
                    Table.AppLayout.DESKTOP_ID + "," +
                    Table.AppLayout.APP_ID + "," +
                    Table.AppLayout.SUB_APP_ID + "," +
                    Table.AppLayout.PACKAGE_NAME + "," +
                    Table.AppLayout.APP_NAME + ") values(");
            sql.append("'").append(Constants.SHORTCUT_BAR_DESKTOP_ID).append("',");
            sql.append("'").append(Constants.DESKTOP_NORMAL_APP_ID_PREFIX).append(appInfo.getId()).append("',");
            sql.append("'").append(Constants.DESKTOP_SUB_APP_ID_PREFIX).append(0).append("',");
            sql.append("'").append(appInfo.getPackageName()).append("',");
            sql.append("'").append(appInfo.getAppName()).append("')");
            mDB.execSQL(sql.toString());
        }
    }
}
