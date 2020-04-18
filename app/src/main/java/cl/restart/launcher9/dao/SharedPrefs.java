package cl.restart.launcher9.dao;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import cl.restart.launcher9.R;

public class SharedPrefs {
    private SharedPreferences mSharedPreferences;

    private SharedPreferences.Editor mEditor;
    private Context mContext;

    public SharedPrefs(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void put(String key, Object object) {
        if (object instanceof String) {
            mEditor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            mEditor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            mEditor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            mEditor.putLong(key, (Long) object);
        } else {
            mEditor.putString(key, object.toString());
        }
        mEditor.commit();
    }

    public void put(int id, Object object) {
        put(mContext.getString(id), object);
    }

    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return mSharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return mSharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return mSharedPreferences.getString(key, null);
        }
    }

    public Object get(int id, Object defaultObject) {
        return get(mContext.getString(id), defaultObject);
    }

    public void remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }

    public void remove(int id) {
        remove(mContext.getString(id));
    }

    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }

    public boolean contain(String key) {
        return mSharedPreferences.contains(key);
    }

    public boolean contain(int id) {
        return contain(mContext.getString(id));
    }

    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
}
