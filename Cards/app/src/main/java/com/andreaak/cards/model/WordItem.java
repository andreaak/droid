package com.andreaak.cards.model;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordItem implements java.io.Serializable, Comparable<WordItem> {

    public static final String TranscriptionSuffix = "_tr";
    public static final String InfoSuffix = "_info";
    public static final String LevelSuffix = "_level";
    public static final String WordClassSuffix = "_wordclass";
    public static final String Rank = "rank";


    private Map<String, String> words = new LinkedHashMap<String, String>();
    private Map<String, String> transcriptions = new HashMap<String, String>();
    private Map<String, String> info = new HashMap<String, String>();

    private int id;
    private String rank;
    private String wordClass;
    private String level;

    public WordItem(int id) {
        this.id = id;
    }

    public String[] getLangs() {
        return words.keySet().toArray(new String[0]);
    }

    public String getValue(String language) {
        return words.get(language);
    }

    public String getTranscription(String language) {
        return transcriptions.get(language + TranscriptionSuffix);
    }

    public String getInfo(String language) {
        return info.get(language + InfoSuffix);
    }

    public String getWordClass() {
        return wordClass;
    }

    public String getLevel() {
        return level;
    }

    public void addItem(String tag, String value) {
        if (tag.endsWith(TranscriptionSuffix)) {
            transcriptions.put(tag, value);
        } else if (tag.endsWith(InfoSuffix)) {
            info.put(tag, value);
        }  else if (Rank.equals(tag)) {
            rank = value;
        } else if (tag.endsWith(WordClassSuffix)) {
            wordClass = value;
        } else if (tag.endsWith(LevelSuffix)) {
            level = value;
        } else if (tag.contains("_")) {

        } else {
            words.put(tag, value);
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(@NonNull WordItem o) {
        return 0;
    }
}
