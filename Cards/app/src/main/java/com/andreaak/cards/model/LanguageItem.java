package com.andreaak.cards.model;

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

    public String getSoundLanguage() {
        return "ru".equals(secondaryLanguage) ? primaryLanguage : secondaryLanguage;
    }

    public LanguageItem(String primaryLanguage, String secondaryLanguage) {
        this.primaryLanguage = primaryLanguage;
        this.secondaryLanguage = secondaryLanguage;
    }

    @Override
    public String toString() {
        return primaryLanguage + SEPARATOR + secondaryLanguage;
    }

    @Override
    public boolean equals(Object object) {
        LanguageItem other = (LanguageItem) object;
        return this.primaryLanguage.equals(other.primaryLanguage)
                && this.secondaryLanguage.equals(other.secondaryLanguage);
    }

    public static LanguageItem getLanguageItem(String lang) {
        String[] langs = lang.split(SEPARATOR);
        return new LanguageItem(langs[0], langs[1]);
    }
}
