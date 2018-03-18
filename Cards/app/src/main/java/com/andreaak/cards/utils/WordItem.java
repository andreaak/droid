package com.andreaak.cards.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordItem implements java.io.Serializable {

    private final String TranscriptionSuffix = "_tr";

    private Map<String, String> words = new LinkedHashMap<String, String>();
    private Map<String, String> transcriptions = new HashMap<String, String>();

    public String[] getLangs() {
        return words.keySet().toArray(new String[0]);
    }

    public String getValue(String language) {
        return words.get(language);
    }

    public String getTranscription(String language) {
        return transcriptions.get(language + TranscriptionSuffix);
    }

    public void addItem(String language, String value) {
        if (language.endsWith(TranscriptionSuffix)) {
            transcriptions.put(language, value);
        } else {
            words.put(language, value);
        }
    }
}
