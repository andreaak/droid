package com.andreaak.cards.model;

import java.io.Serializable;
import java.util.ArrayList;

public class VerbLessonItem implements Serializable {

    public static final String English = "en";
    public static final String Deutsch = "de";

    private String fileName;
    private String path;
    private String language;

    private ArrayList<VerbItem> words = new ArrayList<>();

    public VerbLessonItem(String fileName, String path) {
        this.fileName = fileName;
        language = getLanguage(fileName);
        this.path = path;
    }

    public ArrayList<VerbItem> getWords() {
        return words;
    }

    public String getLanguage() {
        return language;
    }

    private String getLanguage(String fileName) {
        if(fileName.contains("_en")) {
            return English;
        } else if(fileName.contains("_de")) {
            return Deutsch;
        }

        return English;
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
