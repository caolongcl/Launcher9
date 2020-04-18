package cl.restart.launcher9;

import android.content.Intent;

import cl.restart.launcher9.dao.SDao;

public interface AppHelper {
    void startApp(Intent intent);

    void finishApp();

    SDao getDao();
}
