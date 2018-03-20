package com.andreaak.cards;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.cards.predicates.AlwaysTruePredicate;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.predicates.LessonXmlDirectoryPredicate;
import com.andreaak.cards.utils.SharedPreferencesHelper;

import java.io.File;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String RESET_BUTTON = "RESET_BUTTON";
    private final static String OPEN_FOLDER_BUTTON = "OPEN_FOLDER_BUTTON";

    private static final int REQUEST_LOG_DIRECTORY_CHOOSER = 2;

    private Preference resetButton;
    private Preference openFolderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.andreaak.cards.R.xml.settings);

        initButtons();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if(preference == resetButton) {
            Configs.clear();
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(com.andreaak.cards.R.xml.settings);
            initButtons();
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
                    String path = data.getStringExtra(DirectoryChooserActivity.PATH);

                    String logFilePath = SharedPreferencesHelper.getInstance().getString(Configs.SP_LOG_FILE);
                    File file = new File(logFilePath);
                    String newFile = path + "/" + file.getName();
                    SharedPreferencesHelper.getInstance().save(Configs.SP_LOG_FILE, newFile);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initButtons() {
        resetButton = findPreference(RESET_BUTTON);
        resetButton.setOnPreferenceClickListener(this);

        openFolderButton = findPreference(OPEN_FOLDER_BUTTON);
        openFolderButton.setOnPreferenceClickListener(this);
    }

    private void setLogDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_log));
        startActivityForResult(intent, REQUEST_LOG_DIRECTORY_CHOOSER);
    }
}
