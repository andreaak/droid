package com.andreaak.cards.utils;

public class LanguageItem {
    private String primaryLanguage;
    private String secondaryLanguage;

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String getSecondaryLanguage() {
        return secondaryLanguage;
    }

    public LanguageItem(String primaryLanguage, String secondaryLanguage) {
        this.primaryLanguage = primaryLanguage;
        this.secondaryLanguage = secondaryLanguage;
    }
}
