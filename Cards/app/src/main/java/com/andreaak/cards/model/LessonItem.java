package com.andreaak.cards.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LessonItem implements Serializable {

    private String name;
    private String path;

    private LanguageItem languageItem;
    private String currentLanguage;
    private ArrayList<WordItem> words = new ArrayList<>();

    public LessonItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public ArrayList<WordItem> getWords() {
        return words;
    }

    public void add(WordItem word) {
        words.add(word);
    }


    public LanguageItem getLanguageItem() {
        return languageItem;
    }

    public void setLanguageItem(LanguageItem languageItem) {
        this.languageItem = languageItem;
        currentLanguage = languageItem.getPrimaryLanguage();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isContainsWords() {
        return !words.isEmpty();
    }

    public void ToggleLanguage() {
        currentLanguage = currentLanguage.equals(languageItem.getPrimaryLanguage()) ?
                languageItem.getSecondaryLanguage() :
                languageItem.getPrimaryLanguage();
    }

    public void resetLanguage() {
        currentLanguage = languageItem.getPrimaryLanguage();
    }
}