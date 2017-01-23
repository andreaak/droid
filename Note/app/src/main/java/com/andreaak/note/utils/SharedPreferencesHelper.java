package com.andreaak.note.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {

    public static final int NOT_DEFINED_INT = -1;
    public static final boolean NOT_DEFINED_BOOLEAN = false;

    private static SharedPreferencesHelper instance;
    private Context context;

    private SharedPreferencesHelper(Context context) {
        this.context = context;
    }

    public static void initInstance(Context context) {
        instance = new SharedPreferencesHelper(context);
    }

    public static SharedPreferencesHelper getInstance() {
        return instance;
    }

    public boolean save(String id, String value) {
        SharedPreferences sPref = getSharedPreferences();
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(id, value);
        return ed.commit();
    }

    public boolean save(String id, int value) {
        SharedPreferences sPref = getSharedPreferences();
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(id, value);
        return ed.commit();
    }

    public String getString(String id) {
        SharedPreferences sPref = getSharedPreferences();
        return sPref.getString(id, "");
    }

    public int getInt(String id) {
        SharedPreferences sPref = getSharedPreferences();
        return sPref.getInt(id, NOT_DEFINED_INT);
    }

    public boolean getBoolean(String id) {
        SharedPreferences sPref = getSharedPreferences();
        return sPref.getBoolean(id, NOT_DEFINED_BOOLEAN);
    }

    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
