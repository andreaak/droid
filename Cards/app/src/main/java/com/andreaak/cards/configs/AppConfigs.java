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
    private static final String LESSONS_DIR = "Lessons";
    private static final String IRREGULAR_VERB_DIR = "IrregularVerbs";
    // Google
    private static final String SP_GOOGLE_DIR_DEFAULT = "Eng";
    //irregular
    public static final String SP_IRR_VERB_EN_DEFAULT = "irregular_en";
    //
    public static final String SP_TEXT_FONT_SIZE = "SP_TEXT_FONT_SIZE";
    public static final String SP_TRANS_FONT_SIZE = "SP_TRANS_FONT_SIZE";
    public static final String SP_TRANS_SCALE = "SP_TRANS_SCALE";


    //Values
    public static String SoundsDir;
    public static String LessonsExtension;
    public static String LessonsPrefix;
    public static float Scale;

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

        Scale = getConfig(helper, SP_TRANS_SCALE,
                1f);
    }

    public boolean saveSoundsDirectory(String path) {
        SoundsDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DIRECTORY_WITH_SOUNDS_PATH, path);
    }

    public boolean saveScale(float scale) {
        Scale = scale;
        return SharedPreferencesHelper.getInstance().save(SP_TRANS_SCALE, scale);
    }

    public String getLessonsDir() {
        return WorkingDir + "/" + LESSONS_DIR;
    }

    public String getRemoteLessonsDir() {
        return GoogleDir + "/" + LESSONS_DIR;
    }

    public String getIrregularVerbDir() {
        return WorkingDir + "/" + IRREGULAR_VERB_DIR;
    }

    public String getRemoteIrregularVerbDir() {
        return GoogleDir + "/" + IRREGULAR_VERB_DIR;
    }

    protected float getConfig(SharedPreferencesHelper helper, String key, float defaultValue) {
        float value = helper.getFloat(key);
        if ((int) value > (int) SharedPreferencesHelper.NOT_DEFINED_FLOAT) {
            return value;
        } else {
            helper.save(key, defaultValue);
            return defaultValue;
        }
    }
}
