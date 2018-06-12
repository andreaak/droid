package com.andreaak.note.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.common.activitiesShared.DirectoryChooserActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.predicates.AlwaysTruePredicate;
import com.andreaak.note.R;
import com.andreaak.note.configs.AppConfigs;

import java.io.File;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String OPEN_DB_FOLDER_BUTTON = "OPEN_DB_FOLDER_BUTTON";
    private final static String OPEN_DOWNLOAD_FOLDER_BUTTON = "OPEN_DOWNLOAD_FOLDER_BUTTON";
    private final static String OPEN_LOG_FOLDER_BUTTON = "OPEN_LOG_FOLDER_BUTTON";
    private final static String RESET_BUTTON = "RESET_BUTTON";

    private static final int REQUEST_DB_DIRECTORY_CHOOSER = 1;
    private static final int REQUEST_LOG_DIRECTORY_CHOOSER = 2;
    private static final int REQUEST_DOWNLOAD_DIRECTORY_CHOOSER = 3;

    private Preference openDBFolderButton;
    private EditTextPreference dbFolderPref;

    private Preference openDownloadFolderButton;
    private EditTextPreference downloadFolderPref;

    private Preference openLogFolderButton;
    private EditTextPreference logFilePref;
    private Preference resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.andreaak.note.R.layout.activity_settings);

        initPreferences();
    }

    private void initPreferences() {

        openDBFolderButton = findPreference(OPEN_DB_FOLDER_BUTTON);
        openDBFolderButton.setOnPreferenceClickListener(this);
        dbFolderPref = (EditTextPreference) findPreference(AppConfigs.SP_WORKING_DIRECTORY_PATH);

        openDownloadFolderButton = findPreference(OPEN_DOWNLOAD_FOLDER_BUTTON);
        openDownloadFolderButton.setOnPreferenceClickListener(this);
        downloadFolderPref = (EditTextPreference) findPreference(AppConfigs.SP_DOWNLOAD_DIRECTORY_PATH);

        openLogFolderButton = findPreference(OPEN_LOG_FOLDER_BUTTON);
        openLogFolderButton.setOnPreferenceClickListener(this);
        logFilePref = (EditTextPreference) findPreference(AppConfigs.SP_LOG_FILE);

        resetButton = findPreference(RESET_BUTTON);
        resetButton.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference == resetButton) {
            AppConfigs.getInstance().clear();
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(com.andreaak.note.R.layout.activity_settings);
            initPreferences();
            return true;
        } else if (preference == openDBFolderButton) {
            setDBDirectory();
        } else if (preference == openDownloadFolderButton) {
            setDownloadDirectory();
        } else if (preference == openLogFolderButton) {
            setLogDirectory();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOG_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);

                    String logFilePath = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LOG_FILE);
                    File file = new File(logFilePath);
                    String newFile = path + "/" + file.getName();
                    AppConfigs.getInstance().saveLogFile(newFile);
                    logFilePref.setText(newFile);
                }
                break;
            case REQUEST_DB_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    AppConfigs.getInstance().saveWorkingDirectory(path);
                    dbFolderPref.setText(path);
                }
                break;
            case REQUEST_DOWNLOAD_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    AppConfigs.getInstance().saveDownloadDirectory(path);
                    downloadFolderPref.setText(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setDBDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_db_folder));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_DB_DIRECTORY_CHOOSER);
    }

    private void setDownloadDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_download_dir));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().DownloadDir);
        startActivityForResult(intent, REQUEST_DOWNLOAD_DIRECTORY_CHOOSER);
    }

    private void setLogDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_log));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().LogFile);
        startActivityForResult(intent, REQUEST_LOG_DIRECTORY_CHOOSER);
    }
}
