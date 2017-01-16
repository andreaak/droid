package com.andreaak.note.utils;

public class EmailHolder {
    private static final String ACC_NAME = "account_name";

    SharedPreferencesHelper preferences;

    public EmailHolder(SharedPreferencesHelper preferences) {
        this.preferences = preferences;
    }

    private String email = null;

    public void setEmail(String email) {
        this.email = email;
        preferences.save(ACC_NAME, email);
    }

    public String getEmail() {
        return email != null ? email : (email = preferences.read(ACC_NAME));
    }
}
