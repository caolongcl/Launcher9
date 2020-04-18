package cl.restart.launcher9.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "launcher9.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String appLayoutTable = "create table " +
                Table.AppLayout.TABLE_NAME + " (" +
                Table.ID + " integer primary key autoincrement, " +
                Table.AppLayout.DESKTOP_ID + " varchar not null, " +
                Table.AppLayout.APP_ID + " varchar not null, " +
                Table.AppLayout.SUB_APP_ID + " varchar, " +
                Table.AppLayout.PACKAGE_NAME + " text, " +
                Table.AppLayout.APP_NAME + " text)";
        db.execSQL(appLayoutTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
