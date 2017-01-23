package com.andreaak.note;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.note.utils.Configs;

public class PreferencesActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String RESET_BUTTON = "RESET_BUTTON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference button = findPreference(RESET_BUTTON);
        button.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Configs.clear();
//        finish();
//        startActivity(getIntent());
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.preferences);
        Preference button = findPreference(RESET_BUTTON);
        button.setOnPreferenceClickListener(this);
        return true;
    }
}
