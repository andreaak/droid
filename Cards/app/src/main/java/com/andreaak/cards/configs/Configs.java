package com.andreaak.cards.configs;

import android.content.Context;

import static com.andreaak.cards.utils.Utils.isEmpty;

public class Configs {
    //Lessons
    public static final String SP_DIRECTORY_WITH_LESSONS_PATH = "SP_DIRECTORY_WITH_LESSONS_PATH";
    public static final String SP_DIRECTORY_WITH_SOUNDS_PATH = "SP_DIRECTORY_WITH_SOUNDS_PATH";
    //Lessons
    public static final String SP_LAST_LESSON_PATH = "SP_LAST_LESSON_PATH";
    public static final String SP_LAST_LESSON_LANGUAGE = "SP_LAST_LESSON_LANGUAGE";
    //
    private static final String SP_LESSONS_EXTENSION = "SP_LESSONS_EXTENSION";
    private static final String SP_LESSONS_EXTENSION_DEFAULT = ".xml";
    private static final String SP_LESSONS_PREFFIX = "SP_LESSONS_PREFFIX";
    private static final String SP_LESSONS_PREFFIX_DEFAULT = "lesson_";
    // Google
    public static final String SP_GOOGLE_DIR = "SP_GOOGLE_DIR";
    private static final String SP_GOOGLE_DIR_DEFAULT = "Eng";
    //irregular
    public static final String SP_IRR_VERB_EN_DEFAULT = "irregular_en";
    //Log
    public static final String SP_LOG_FILE = "SP_LOG_FILE";
    public static final String SP_IS_LOGGING_ACTIVE = "SP_IS_LOGGING_ACTIVE";
    private static final String SP_LOG_FILE_DEFAULT = "/log.file";

    //
    public static final String SP_TEXT_FONT_SIZE = "SP_TEXT_FONT_SIZE";
    public static final String SP_TRANS_FONT_SIZE = "SP_TRANS_FONT_SIZE";

    //Values
    public static String LessonDir;
    public static String SoundsDir;
    public static String GoogleDir;
    public static String LessonsExtension;
    public static String LessonsPrefix;
    public static String LogFile;
    public static boolean IsLoggingActive;
    private static String FilesDefaultLocation;

    public static void init(Context context) {
        FilesDefaultLocation = context.getFilesDir().getPath();
    }

    public static void read() {
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        LessonDir = getConfig(helper, SP_DIRECTORY_WITH_LESSONS_PATH,
                FilesDefaultLocation);

        SoundsDir = getConfig(helper, SP_DIRECTORY_WITH_SOUNDS_PATH,
                FilesDefaultLocation);

        LessonsExtension = getConfig(helper, SP_LESSONS_EXTENSION,
                SP_LESSONS_EXTENSION_DEFAULT);

        LessonsPrefix = getConfig(helper, SP_LESSONS_PREFFIX,
                SP_LESSONS_PREFFIX_DEFAULT);

        LogFile = getConfig(helper, SP_LOG_FILE,
                FilesDefaultLocation + SP_LOG_FILE_DEFAULT);

        GoogleDir = getConfig(helper, SP_GOOGLE_DIR,
                SP_GOOGLE_DIR_DEFAULT);

        IsLoggingActive = helper.getBoolean(SP_IS_LOGGING_ACTIVE);
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

    public static boolean saveLessonsDirectory(String path) {
        LessonDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DIRECTORY_WITH_LESSONS_PATH, path);
    }

    public static boolean saveSoundsDirectory(String path) {
        SoundsDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DIRECTORY_WITH_SOUNDS_PATH, path);
    }

    public static void clear() {
        SharedPreferencesHelper.getInstance().getSharedPreferences().edit().clear().commit();
        read();
    }
}
