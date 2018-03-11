package com.andreaak.cards.utils;

import java.util.ArrayList;

public class LessonItem {

    private String name;

    private ArrayList<WordItem> words = new ArrayList<>();

    public ArrayList<WordItem> getWords() {
        return words;
    }

    public LessonItem(String name) {
        this.name = name;
    }

    public void add(WordItem word) {
        words.add(word);
    }
}
