package com.andreaak.cards.model;

import com.andreaak.common.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;

public class LessonItem implements Serializable {

    private String name;
    private String fileName;
    private String path;

    private LanguageItem languageItem;
    private String currentLanguage;
    private ArrayList<WordItem> words = new ArrayList<>();

    public LessonItem(String fileName, String path) {
        this.fileName = fileName;
        this.name = Utils.getFileNameWithoutExtensions(fileName);
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

    public String getFileName() {
        return fileName;
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

    public void changeWord(WordItem word) {

        for (int i = 0; i < words.size(); i++) {
            WordItem existWord = words.get(i);
            if (existWord.getId() == word.getId()) {
                words.set(i, word);
                break;
            }
        }
    }
}
