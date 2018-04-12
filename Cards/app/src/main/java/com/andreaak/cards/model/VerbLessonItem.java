package com.andreaak.cards.model;

import java.io.Serializable;
import java.util.ArrayList;

public class VerbLessonItem implements Serializable {

    private String fileName;
    private String path;

    private ArrayList<VerbItem> words = new ArrayList<>();

    public VerbLessonItem(String fileName, String path) {
        this.fileName = fileName;
        this.path = path;
    }

    public ArrayList<VerbItem> getWords() {
        return words;
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
