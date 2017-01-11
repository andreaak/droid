package com.andreaak.note.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesHelper {

    public static final int NOT_DEFINED_INT = -1;
    private static final String PREFS_NAME = "PREFS_NAME";


    private static SharedPreferencesHelper instance;
    private Context context;

    public static void initInstance(Context context) {
        instance = new SharedPreferencesHelper(context);
    }

    public static SharedPreferencesHelper getInstance() {
        return instance;
    }

    private SharedPreferencesHelper(Context context) {
        this.context = context;
    }

    public boolean save(String id, String value) {
        SharedPreferences sPref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(id, value);
        return ed.commit();
    }

    public boolean save(String id, int value) {
        SharedPreferences sPref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(id, value);
        return ed.commit();
    }

    public String read(String id) {
        SharedPreferences sPref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sPref.getString(id, "");
    }

    public int readInt(String id) {
        SharedPreferences sPref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sPref.getInt(id, NOT_DEFINED_INT);
    }
}
