package com.andreaak.cards.configs;

import com.andreaak.common.configs.SharedPreferencesHelper;

public class AppConfigs extends com.andreaak.common.configs.Configs {
    //Lessons
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
    private static final String SP_GOOGLE_DIR_DEFAULT = "Eng";
    //irregular
    public static final String SP_IRR_VERB_EN_DEFAULT = "irregular_en";
    //
    public static final String SP_TEXT_FONT_SIZE = "SP_TEXT_FONT_SIZE";
    public static final String SP_TRANS_FONT_SIZE = "SP_TRANS_FONT_SIZE";

    //Values
    public static String SoundsDir;
    public static String LessonsExtension;
    public static String LessonsPrefix;

    public static AppConfigs getInstance() {
        if (instance == null) {
            instance = new AppConfigs();
        }
        return (AppConfigs) instance;
    }

    public void read() {
        super.read();

        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        SoundsDir = getConfig(helper, SP_DIRECTORY_WITH_SOUNDS_PATH,
                FilesDefaultLocation);

        LessonsExtension = getConfig(helper, SP_LESSONS_EXTENSION,
                SP_LESSONS_EXTENSION_DEFAULT);

        LessonsPrefix = getConfig(helper, SP_LESSONS_PREFFIX,
                SP_LESSONS_PREFFIX_DEFAULT);

        GoogleDir = getConfig(helper, SP_GOOGLE_DIR,
                SP_GOOGLE_DIR_DEFAULT);
    }

    public boolean saveSoundsDirectory(String path) {
        SoundsDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DIRECTORY_WITH_SOUNDS_PATH, path);
    }
}
