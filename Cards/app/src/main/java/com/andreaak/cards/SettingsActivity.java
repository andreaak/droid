package com.andreaak.cards;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.andreaak.cards.utils.Configs;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    private final static String RESET_BUTTON = "RESET_BUTTON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.andreaak.cards.R.xml.settings);

        Preference button = findPreference(RESET_BUTTON);
        button.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Configs.clear();
//        finish();
//        startActivity(getIntent());
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(com.andreaak.cards.R.xml.settings);
        Preference button = findPreference(RESET_BUTTON);
        button.setOnPreferenceClickListener(this);
        return true;
    }
}
