package com.andreaak.note.utils;

import android.os.Environment;

public class Configs {
    public static final String GOOGLE_ACCOUNT_NAME = "GOOGLE_ACCOUNT_NAME";
    public static final String DOWNLOAD_DIR_PATH = "DOWNLOAD_DIR_PATH";
    public static final String DIRECTORY_WITH_DB_PATH = "DIRECTORY_WITH_DB_PATH";
    public static final String TEXT_ZOOM = "TEXT_ZOOM";
    public static final String DATABASE_EXTENSION = ".db";
    public static String LOG_FILE = Environment.getExternalStorageDirectory().getPath() + "/log.file";
}
