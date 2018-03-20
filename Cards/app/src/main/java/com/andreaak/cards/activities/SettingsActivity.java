package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.cards.R;
import com.andreaak.cards.predicates.AlwaysTruePredicate;
import com.andreaak.cards.activitiesShared.DirectoryChooserActivity;
import com.andreaak.cards.configs.Configs;
import com.andreaak.cards.configs.SharedPreferencesHelper;

import java.io.File;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String RESET_BUTTON = "RESET_BUTTON";
    private final static String OPEN_FOLDER_BUTTON = "OPEN_FOLDER_BUTTON";
    private final static String SP_LOG_FILE = "SP_LOG_FILE";

    private static final int REQUEST_LOG_DIRECTORY_CHOOSER = 2;

    private Preference resetButton;
    private Preference openFolderButton;
    private EditTextPreference logFilePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.andreaak.cards.R.xml.settings);

        initPreferences();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference == resetButton) {
            Configs.clear();
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(com.andreaak.cards.R.xml.settings);
            initPreferences();
            return true;
        } else if(preference == openFolderButton) {
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

                    String logFilePath = SharedPreferencesHelper.getInstance().getString(Configs.SP_LOG_FILE);
                    File file = new File(logFilePath);
                    String newFile = path + "/" + file.getName();
                    SharedPreferencesHelper.getInstance().save(Configs.SP_LOG_FILE, newFile);
                    logFilePref.setText(newFile);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initPreferences() {
        resetButton = findPreference(RESET_BUTTON);
        resetButton.setOnPreferenceClickListener(this);

        openFolderButton = findPreference(OPEN_FOLDER_BUTTON);
        openFolderButton.setOnPreferenceClickListener(this);

        logFilePref = (EditTextPreference)findPreference(SP_LOG_FILE);
    }

    private void setLogDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_log));
        startActivityForResult(intent, REQUEST_LOG_DIRECTORY_CHOOSER);
    }
}
