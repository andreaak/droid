package com.andreaak.note.configs;

import com.andreaak.common.configs.Configs;
import com.andreaak.common.configs.SharedPreferencesHelper;

public class AppConfigs extends Configs {
    //Keys
    public static final String SP_TEXT_ZOOM = "SP_TEXT_ZOOM";
    //
    private static final String SP_DATABASE_EXTENSION = "SP_DATABASE_EXTENSION";
    //
    private static final String SP_GOOGLE_DIR_DEFAULT = "DB";
    private static final String SP_DATABASE_EXTENSION_DEFAULT = ".db";
    //
    public String DatabaseExtension;

    public static AppConfigs getInstance() {
        if (instance == null) {
            instance = new AppConfigs();
        }
        return (AppConfigs) instance;
    }

    public void read() {
        super.read();
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance();

        DatabaseExtension = getConfig(helper, SP_DATABASE_EXTENSION,
                SP_DATABASE_EXTENSION_DEFAULT);

        GoogleDir = getConfig(helper, SP_GOOGLE_DIR,
                SP_GOOGLE_DIR_DEFAULT);
    }
}
