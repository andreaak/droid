package com.andreaak.cards.model;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordItem implements java.io.Serializable, Comparable<WordItem> {

    public static final String TranscriptionSuffix = "_tr";
    public static final String InfoSuffix = "_info";
    public static final String Rank = "rank";


    private Map<String, String> words = new LinkedHashMap<String, String>();
    private Map<String, String> transcriptions = new HashMap<String, String>();
    private Map<String, String> info = new HashMap<String, String>();

    private int id;
    private String rank;

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

    public void addItem(String language, String value) {
        if (language.endsWith(TranscriptionSuffix)) {
            transcriptions.put(language, value);
        } else if (language.endsWith(InfoSuffix)) {
            info.put(language, value);
        }  else if (Rank.equals(language)) {
            rank = value;
        } else {
            words.put(language, value);
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
