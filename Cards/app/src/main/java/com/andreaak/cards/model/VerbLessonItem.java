package com.andreaak.cards.model;

import com.andreaak.cards.configs.Lang;
import com.andreaak.common.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;

public class VerbLessonItem implements Serializable {

    private String fileName;
    private String path;
    private String language;
    private String displayName;

    private ArrayList<VerbItem> words = new ArrayList<>();

    public VerbLessonItem(String fileName, String path) {
        this.fileName = fileName;
        language = getLanguage(fileName);
        this.path = path;
        this.displayName = Utils.getDisplayName(fileName, "irregular_");
    }

    public ArrayList<VerbItem> getWords() {
        return words;
    }

    public String getLanguage() {
        return language;
    }

    private String getLanguage(String fileName) {
        if(fileName.contains("_en")) {
            return Lang.EN.getCode();
        } else if(fileName.contains("_de")) {
            return Lang.DE.getCode();
        }

        return Lang.EN.getCode();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void add(VerbItem word) {
        words.add(word);
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public boolean isContainsWords() {
        return !words.isEmpty();
    }

    public void changeWord(VerbItem word) {

        for (int i = 0; i < words.size(); i++) {
            VerbItem existWord = words.get(i);
            if (existWord.getId() == word.getId()) {
                words.set(i, word);
                break;
            }
        }
    }
}
