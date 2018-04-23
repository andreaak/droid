package com.andreaak.cards.configs;

import android.content.Context;

import static com.andreaak.cards.utils.Utils.isEmpty;

public class Configs {
    //Keys
    public static final String SP_DIRECTORY_WITH_LESSONS_PATH = "SP_DIRECTORY_WITH_LESSONS_PATH";
    //
    public static final String SP_GOOGLE_DIR = "SP_GOOGLE_DIR";
    private static final String SP_GOOGLE_DIR_DEFAULT = "Eng";
    //
    public static final String SP_LESSONS_EXTENSION = "SP_LESSONS_EXTENSION";
    private static final String SP_LESSONS_EXTENSION_DEFAULT = ".xml";
    public static final String SP_LESSONS_PREFFIX = "SP_LESSONS_PREFFIX";
    private static final String SP_LESSONS_PREFFIX_DEFAULT = "lesson_";
    //
    public static final String SP_IRR_VERB_EN_DEFAULT = "irregular_en";
    //
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    public static final String SP_IS_LOGGING_ACTIVE = "SP_IS_LOGGING_ACTIVE";
    public static final String SP_LOG_FILE_DEFAULT = "/log.file";

    //
    public static final String SP_TEXT_FONT_SIZE = "SP_TEXT_FONT_SIZE";
    public static final String SP_TRANS_FONT_SIZE = "SP_TRANS_FONT_SIZE";
    //
    public static final String SP_LAST_LESSON_PATH = "SP_LAST_LESSON_PATH";
    public static final String SP_LAST_LESSON_LANGUAGE = "SP_LAST_LESSON_LANGUAGE";
    //Values
    public static String LessonDir;
    public static String GoogleDir;
    public static String LessonsExtension;
    public static String LessonsPreffix;
    public static String LogFile;
    public static boolean IsLoggingActive;
    private static String FilesDefaultLocation;

    public static void init(Context context) {
        FilesDefaultLocation = context.getFilesDir().getPath();
    }

    public static void read() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        String temp = helper.getString(SP_DIRECTORY_WITH_LESSONS_PATH);
        if (!isEmpty(temp)) {
            LessonDir = temp;
        } else {
            LessonDir = FilesDefaultLocation;
            helper.save(SP_DIRECTORY_WITH_LESSONS_PATH, LessonDir);
        }

        temp = helper.getString(SP_GOOGLE_DIR);
        if (!isEmpty(temp)) {
            GoogleDir = temp;
        } else {
            GoogleDir = SP_GOOGLE_DIR_DEFAULT;
            helper.save(SP_GOOGLE_DIR, GoogleDir);
        }

        temp = helper.getString(SP_LESSONS_EXTENSION);
        if (!isEmpty(temp)) {
            LessonsExtension = temp;
        } else {
            LessonsExtension = SP_LESSONS_EXTENSION_DEFAULT;
            helper.save(SP_LESSONS_EXTENSION, LessonsExtension);
        }

        temp = helper.getString(SP_LESSONS_PREFFIX);
        if (!isEmpty(temp)) {
            LessonsPreffix = temp;
        } else {
            LessonsPreffix = SP_LESSONS_PREFFIX_DEFAULT;
            helper.save(SP_LESSONS_PREFFIX, LessonsPreffix);
        }

        temp = helper.getString(SP_LOG_FILE);
        if (!isEmpty(temp)) {
            LogFile = temp;
        } else {
            LogFile = FilesDefaultLocation + SP_LOG_FILE_DEFAULT;
            helper.save(SP_LOG_FILE, LogFile);
        }

        IsLoggingActive = helper.getBoolean(SP_IS_LOGGING_ACTIVE);
    }

    public static void clear() {
        SharedPreferencesHelper.getInstance().getSharedPreferences().edit().clear().commit();
        read();
    }
}
