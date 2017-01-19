package com.andreaak.note.utils;

import android.os.Environment;
import static com.andreaak.note.utils.Utils.*;

public class Configs {
    //pref
    public static final String SP_DOWNLOAD_DIR_PATH = "SP_DOWNLOAD_DIR_PATH";
    public static final String SP_DIRECTORY_WITH_DB_PATH = "SP_DIRECTORY_WITH_DB_PATH";
    public static final String SP_TEXT_ZOOM = "SP_TEXT_ZOOM";
    public static final String SP_GOOGLE_DIR = "SP_GOOGLE_DIR";
    public static final String SP_DATABASE_EXTENSION = "SP_DATABASE_EXTENSION";
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    //
    public static String GOOGLE_DIR = "DB";
    public static String DATABASE_EXTENSION = ".db";
    public static String LOG_FILE = Environment.getExternalStorageDirectory().getPath() + "/log.file";

    public static void read() {
        String temp = SharedPreferencesHelper.getInstance().read(SP_GOOGLE_DIR);
        if(!isEmpty(temp)) {
            GOOGLE_DIR = temp;
        }

        temp = SharedPreferencesHelper.getInstance().read(SP_DATABASE_EXTENSION);
        if(!isEmpty(temp)) {
            DATABASE_EXTENSION = temp;
        }

        temp = SharedPreferencesHelper.getInstance().read(SP_LOG_FILE);
        if(!isEmpty(temp)) {
            LOG_FILE = temp;
        }
    }
}
