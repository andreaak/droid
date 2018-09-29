package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.activitiesShared.DirectoryChooserActivity;
import com.andreaak.common.predicates.AlwaysTruePredicate;

import java.io.File;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String OPEN_WORKING_FOLDER_BUTTON = "OPEN_WORKING_FOLDER_BUTTON";
    private final static String OPEN_SOUNDS_FOLDER_BUTTON = "OPEN_SOUNDS_FOLDER_BUTTON";
    private final static String OPEN_LOG_FOLDER_BUTTON = "OPEN_LOG_FOLDER_BUTTON";
    private final static String RESET_BUTTON = "RESET_BUTTON";

    private static final int REQUEST_WORKING_DIRECTORY_CHOOSER = 1;
    private static final int REQUEST_LOG_DIRECTORY_CHOOSER = 2;
    private static final int REQUEST_SOUNDS_DIRECTORY_CHOOSER = 3;

    private Preference openWorkingFolderButton;
    private EditTextPreference workingFolderPref;
    private Preference openSoundsFolderButton;
    private EditTextPreference soundsFolderPref;
    private Preference openLogFolderButton;
    private EditTextPreference logFilePref;
    private Preference resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.andreaak.cards.R.layout.activity_settings);

        initPreferences();
    }

    private void initPreferences() {
        openWorkingFolderButton = findPreference(OPEN_WORKING_FOLDER_BUTTON);
        openWorkingFolderButton.setOnPreferenceClickListener(this);
        workingFolderPref = (EditTextPreference) findPreference(AppConfigs.SP_WORKING_DIRECTORY_PATH);
        workingFolderPref.setSummary(AppConfigs.getInstance().WorkingDir);

        openSoundsFolderButton = findPreference(OPEN_SOUNDS_FOLDER_BUTTON);
        openSoundsFolderButton.setOnPreferenceClickListener(this);
        soundsFolderPref = (EditTextPreference) findPreference(AppConfigs.SP_DIRECTORY_WITH_SOUNDS_PATH);
        soundsFolderPref.setSummary(AppConfigs.getInstance().SoundsDir);

        openLogFolderButton = findPreference(OPEN_LOG_FOLDER_BUTTON);
        openLogFolderButton.setOnPreferenceClickListener(this);
        logFilePref = (EditTextPreference) findPreference(AppConfigs.SP_LOG_FILE);
        logFilePref.setSummary(AppConfigs.getInstance().LogFile);

        resetButton = findPreference(RESET_BUTTON);
        resetButton.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference == resetButton) {
            AppConfigs.getInstance().clear();
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(com.andreaak.cards.R.layout.activity_settings);
            initPreferences();
            return true;
        } else if (preference == openWorkingFolderButton) {
            setWorkingDirectory();
        } else if (preference == openSoundsFolderButton) {
            setSoundsDirectory();
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

                    String logFilePath = AppConfigs.getInstance().LogFile;
                    File file = new File(logFilePath);
                    String newFile = path + "/" + file.getName();
                    AppConfigs.getInstance().saveLogFile(newFile);
                    logFilePref.setText(newFile);
                }
                break;
            case REQUEST_WORKING_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    AppConfigs.getInstance().saveWorkingDirectory(path);
                    workingFolderPref.setText(path);
                }
                break;
            case REQUEST_SOUNDS_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    AppConfigs.getInstance().saveSoundsDirectory(path);
                    soundsFolderPref.setText(path);
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

    private void setWorkingDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_working_directory));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_WORKING_DIRECTORY_CHOOSER);
    }

    private void setSoundsDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_sounds_folder));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, AppConfigs.getInstance().SoundsDir);
        startActivityForResult(intent, REQUEST_SOUNDS_DIRECTORY_CHOOSER);
    }
}
