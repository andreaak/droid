package com.andreaak.note.google;

import com.andreaak.note.utils.Configs;
import com.andreaak.note.utils.SharedPreferencesHelper;

public class EmailHolder {

    SharedPreferencesHelper preferences;
    private String email = null;

    public EmailHolder(SharedPreferencesHelper preferences) {
        this.preferences = preferences;
    }

    public String getEmail() {
        return email != null ? email : (email = preferences.read(Configs.GOOGLE_ACCOUNT_NAME));
    }

    public void setEmail(String email) {
        this.email = email;
        preferences.save(Configs.GOOGLE_ACCOUNT_NAME, email);
    }
}
