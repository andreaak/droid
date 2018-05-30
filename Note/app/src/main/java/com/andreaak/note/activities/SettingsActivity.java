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

    private final static String OPEN_LOG_FOLDER_BUTTON = "OPEN_LOG_FOLDER_BUTTON";
    private final static String RESET_BUTTON = "RESET_BUTTON";

    private static final int REQUEST_LOG_DIRECTORY_CHOOSER = 2;

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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLogDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_log));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().LogFile);
        startActivityForResult(intent, REQUEST_LOG_DIRECTORY_CHOOSER);
    }
}
