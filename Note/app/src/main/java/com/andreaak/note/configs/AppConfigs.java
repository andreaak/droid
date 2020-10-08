package com.andreaak.note.configs;

import com.andreaak.common.configs.Configs;
import com.andreaak.common.configs.SharedPreferencesHelper;

public class AppConfigs extends Configs {
    //
    public static final String SP_DOWNLOAD_DIRECTORY_PATH = "SP_DOWNLOAD_DIRECTORY_PATH";
    //
    public static final String SP_TEXT_BACK = "SP_TEXT_BACK";
    public static final String SP_TEXT_ZOOM = "SP_TEXT_ZOOM";
    //
    private static final String SP_DATABASE_EXTENSION = "SP_DATABASE_EXTENSION";
    //
    private static final String SP_GOOGLE_DIR_DEFAULT = "DB";
    private static final String SP_DATABASE_EXTENSION_DEFAULT = ".db";
    //
    public String DatabaseExtension;
    public String DownloadDir;

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

        DownloadDir = getConfig(helper, SP_DOWNLOAD_DIRECTORY_PATH,
                "");

        GoogleDir = getConfig(helper, SP_GOOGLE_DIR,
                SP_GOOGLE_DIR_DEFAULT);
    }

    public boolean saveDownloadDirectory(String path) {
        this.DownloadDir = path;
        return SharedPreferencesHelper.getInstance().save(SP_DOWNLOAD_DIRECTORY_PATH, path);
    }
}
