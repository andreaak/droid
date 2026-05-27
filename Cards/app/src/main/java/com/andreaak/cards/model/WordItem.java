package com.andreaak.cards.model;

import androidx.annotation.NonNull;

import com.andreaak.common.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordItem implements java.io.Serializable, Comparable<WordItem> {

    public static final String TranscriptionSuffix = "_tr";
    public static final String InfoSuffix = "_info";
    public static final String ExampleSuffix = "_example";
    public static final String DescriptionSuffix = "_description";
    public static final String GPTDescriptionSuffix = "_gptdescription";
    public static final String LevelSuffix = "_level";
    public static final String WordClassSuffix = "_wordclass";
    public static final String PrapSuffix = "_prap";
    public static final String Rank = "rank";


    protected Map<String, String> words = new LinkedHashMap<String, String>();
    private Map<String, String> transcriptions = new HashMap<String, String>();
    protected Map<String, String> info = new HashMap<String, String>();
    private Map<String, String> level = new HashMap<String, String>();
    private Map<String, String> example = new HashMap<String, String>();
    private Map<String, String> descriptions = new HashMap<String, String>();
    private Map<String, String> gptdescriptions = new HashMap<String, String>();
    private Map<String, String> praps = new HashMap<String, String>();

    private int id;
    private String rank;
    protected String wordClass;

    public WordItem(int id) {
        this.id = id;
    }

    public String getDisplayName(String lang) {
        return "-----";
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
        String value = info.get(language + InfoSuffix);

        return value == null ? "" : value;
    }

    public String getExample(String language) {
        return example.get(language + ExampleSuffix);
    }

    public String getPrap(String language) {
        return praps.get(language + PrapSuffix);
    }

    public String getDescription(String language) {
        return descriptions.get(language + DescriptionSuffix);
    }

    public String getGPTDescription(String language) {
        return gptdescriptions.get(language + GPTDescriptionSuffix);
    }

    public String getWordClass() {
        return wordClass;
    }

    public String getLevel(String language) {

        return level.get(language + LevelSuffix);
    }

    public String getPath() {
        return "";
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
            level.put(tag, value);
        } else if (tag.endsWith(ExampleSuffix)) {
            example.put(tag, value);
        } else if (tag.endsWith(DescriptionSuffix)) {
            descriptions.put(tag, value);
        }else if (tag.endsWith(GPTDescriptionSuffix)) {
            gptdescriptions.put(tag, value);
        } else if (tag.endsWith(PrapSuffix)) {
            praps.put(tag, value);
        } else if (tag.contains("_")) {

        } else {
            words.put(tag, value);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull WordItem o) {
        return 0;
    }
}
