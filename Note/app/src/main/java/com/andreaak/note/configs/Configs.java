package com.andreaak.note.configs;

import android.content.Context;

import static com.andreaak.note.utils.Utils.isEmpty;

public class Configs {
    //Keys
    private static final String SP_DIRECTORY_WITH_DB_PATH = "SP_DIRECTORY_WITH_DB_PATH";
    public static final String SP_TEXT_ZOOM = "SP_TEXT_ZOOM";
    //
    public static final String SP_GOOGLE_DIR = "SP_GOOGLE_DIR";
    public static final String SP_DATABASE_EXTENSION = "SP_DATABASE_EXTENSION";
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    public static final String SP_IS_LOGGING_ACTIVE = "SP_IS_LOGGING_ACTIVE";
    //
    private static final String SP_GOOGLE_DIR_DEF = "DB";
    private static final String SP_DATABASE_EXTENSION_DEF = ".db";
    //
    private static final String SP_LOG_FILE_DEFAULT = "/log.file";
    //Values
    private static String FilesDefaultLocation;
    //
    public static String FilesDir;
    public static String DatabaseExtension;
    //
    public static String GoogleDir;
    //
    public static String LogFile;
    public static boolean IsLoggingActive;



    public static void init(Context context) {
        FilesDefaultLocation = context.getFilesDir().getPath();
    }

    public static void read() {

        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        FilesDir = getConfig(helper, SP_DIRECTORY_WITH_DB_PATH,
                FilesDefaultLocation);

        DatabaseExtension = getConfig(helper, SP_DATABASE_EXTENSION,
                SP_DATABASE_EXTENSION_DEF);

        GoogleDir = getConfig(helper, SP_GOOGLE_DIR,
                SP_GOOGLE_DIR_DEF);

        LogFile = getConfig(helper, SP_LOG_FILE,
                FilesDefaultLocation + SP_LOG_FILE_DEFAULT);

        IsLoggingActive = helper.getBoolean(SP_IS_LOGGING_ACTIVE);
    }

    public static boolean saveFilesDirectory(String path) {
        FilesDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DIRECTORY_WITH_DB_PATH, path);
    }

    private static String getConfig(SharedPreferencesHelper helper, String key, String defaultValue) {
        String value = helper.getString(key);
        if (!isEmpty(value)) {
            return value;
        } else {
            helper.save(key, defaultValue);
            return defaultValue;
        }
    }

    public static void clear() {
        SharedPreferencesHelper.getInstance().getSharedPreferences().edit().clear().commit();
        read();
    }
}
