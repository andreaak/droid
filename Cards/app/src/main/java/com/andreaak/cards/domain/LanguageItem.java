package com.andreaak.cards.domain;

public class LanguageItem implements java.io.Serializable {
    public static final String SEPARATOR = "__";

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

    @Override
    public String toString() {
        return primaryLanguage + SEPARATOR + secondaryLanguage;
    }

    public static LanguageItem getItem(String lang) {
        String[] langs = lang.split(SEPARATOR);
        return new LanguageItem(langs[0], langs[1]);
    }
}
