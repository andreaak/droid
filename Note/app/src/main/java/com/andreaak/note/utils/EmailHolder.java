package com.andreaak.note.utils;

public class EmailHolder {
    private static final String GOOGLE_ACCOUNT_NAME = "GOOGLE_ACCOUNT_NAME";

    SharedPreferencesHelper preferences;

    public EmailHolder(SharedPreferencesHelper preferences) {
        this.preferences = preferences;
    }

    private String email = null;

    public void setEmail(String email) {
        this.email = email;
        preferences.save(GOOGLE_ACCOUNT_NAME, email);
    }

    public String getEmail() {
        return email != null ? email : (email = preferences.read(GOOGLE_ACCOUNT_NAME));
    }
}
