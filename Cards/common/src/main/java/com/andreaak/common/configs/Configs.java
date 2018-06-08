package com.andreaak.common.configs;

import android.content.Context;

import static com.andreaak.common.utils.Utils.isEmpty;

public class Configs {
    //Working directory
    public static final String SP_WORKING_DIRECTORY_PATH = "SP_WORKING_DIRECTORY_PATH";
    // Google
    public static final String SP_GOOGLE_DIR = "SP_GOOGLE_DIR";
    //Log
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    private static final String SP_IS_LOGGING_ACTIVE = "SP_IS_LOGGING_ACTIVE";
    private static final String SP_LOG_FILE_DEFAULT = "/log.file";

    protected static Configs instance;
    public String WorkingDir;
    public String GoogleDir;
    public String LogFile;
    public boolean IsLoggingActive;
    protected String FilesDefaultLocation;

    public static Configs getInstance() {
        if (instance == null) {
            instance = new Configs();
        }
        return instance;
    }

    public void init(Context context) {
        FilesDefaultLocation = context.getFilesDir().getPath();
    }

    public void read() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        WorkingDir = getConfig(helper, SP_WORKING_DIRECTORY_PATH,
                FilesDefaultLocation);

        LogFile = getConfig(helper, SP_LOG_FILE,
                FilesDefaultLocation + SP_LOG_FILE_DEFAULT);

        IsLoggingActive = helper.getBoolean(SP_IS_LOGGING_ACTIVE);
    }

    protected String getConfig(SharedPreferencesHelper helper, String key, String defaultValue) {
        String value = helper.getString(key);
        if (!isEmpty(value)) {
            return value;
        } else {
            helper.save(key, defaultValue);
            return defaultValue;
        }
    }

    public boolean saveWorkingDirectory(String path) {
        WorkingDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_WORKING_DIRECTORY_PATH, path);
    }

    public boolean saveLogFile(String path) {
        LogFile = path;
        return SharedPreferencesHelper.getInstance().save(SP_LOG_FILE, path);
    }

    public void clear() {
        SharedPreferencesHelper.getInstance().getSharedPreferences().edit().clear().commit();
        read();
    }
}
