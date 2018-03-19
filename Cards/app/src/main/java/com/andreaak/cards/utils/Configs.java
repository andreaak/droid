package com.andreaak.cards.utils;

import android.content.Context;

import static com.andreaak.cards.utils.Utils.isEmpty;

public class Configs {
    //Keys
    public static final String SP_DOWNLOAD_DIR_PATH = "SP_DOWNLOAD_DIR_PATH";
    public static final String SP_DIRECTORY_WITH_DB_PATH = "SP_DIRECTORY_WITH_DB_PATH";
    public static final String SP_TEXT_ZOOM = "SP_TEXT_ZOOM";
    //
    public static final String SP_GOOGLE_CARD_DIR = "SP_GOOGLE_CARD_DIR";
    public static final String SP_LESSONS_EXTENSION = "SP_LESSONS_EXTENSION";
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    public static final String SP_IS_LOGGING_ACTIVE = "SP_IS_LOGGING_ACTIVE";
    //
    private static final String SP_GOOGLE_CARD_DIR_DEF = "Eng";
    private static final String SP_LESSONS_EXTENSION_DEF = ".xml";
    //
    public static final String SP_TEXT_FONT_SIZE = "SP_TEXT_FONT_SIZE";
    public static final String SP_TRANS_FONT_SIZE = "SP_TRANS_FONT_SIZE";
    //
    public static final String SP_LAST_LESSON_PATH = "SP_LAST_LESSON_PATH";
    public static final String SP_LAST_LESSON_LANGUAGE = "SP_LAST_LESSON_LANGUAGE";
    //Values
    public static String GoogleDir;
    public static String LessonsExtension;
    public static String LogFile;
    public static boolean IsLoggingActive;
    private static String LogFileDefault = "/log.file";

    public static void init(Context context) {
        LogFileDefault = context.getFilesDir() + LogFileDefault;
    }

    public static void read() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        String temp = helper.getString(SP_GOOGLE_CARD_DIR);
        if (!isEmpty(temp)) {
            GoogleDir = temp;
        } else {
            GoogleDir = SP_GOOGLE_CARD_DIR_DEF;
            helper.save(SP_GOOGLE_CARD_DIR, SP_GOOGLE_CARD_DIR_DEF);
        }

        temp = helper.getString(SP_LESSONS_EXTENSION);
        if (!isEmpty(temp)) {
            LessonsExtension = temp;
        } else {
            LessonsExtension = SP_LESSONS_EXTENSION_DEF;
            helper.save(SP_LESSONS_EXTENSION, SP_LESSONS_EXTENSION_DEF);
        }

        temp = helper.getString(SP_LOG_FILE);
        if (!isEmpty(temp)) {
            LogFile = temp;
        } else {
            LogFile = LogFileDefault;
            helper.save(SP_LOG_FILE, LogFileDefault);
        }

        IsLoggingActive = helper.getBoolean(SP_IS_LOGGING_ACTIVE);
    }

    public static void clear() {
        SharedPreferencesHelper.getInstance().getSharedPreferences().edit().clear().commit();
        read();
    }
}
