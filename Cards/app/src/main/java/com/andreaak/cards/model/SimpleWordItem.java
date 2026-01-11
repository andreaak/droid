package com.andreaak.cards.model;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.andreaak.common.utils.Utils;

import java.util.Arrays;
import java.util.Objects;

public class SimpleWordItem extends WordItem implements java.io.Serializable {

    public static final String TranscriptionSuffix = "_tr";
    public static final String InfoSuffix = "_info";
    public static final String ExampleSuffix = "_example";
    public static final String DescriptionSuffix = "_description";
    public static final String LevelSuffix = "_level";
    public static final String WordClassSuffix = "_wordclass";
    public static final String Rank = "rank";

    private String path;

    public SimpleWordItem(String path) {
        super(0);
        this.path = path;
    }

    public String getDisplayName(String lang) {
        String wc = getWordClass();
        if(Utils.isEmpty(wc)) {
            return getValue(lang);
        }
        return (getValue(lang) + " " + getWordClass() +
                ("verb".equals(wc) ? (" " + getInfo(lang)) : "")).trim();
    }

    public String getPath() {
        return path;
    }

    @Override
    public void addItem(String tag, String value) {
        if (tag.endsWith(TranscriptionSuffix)) {

        } else if (tag.endsWith(InfoSuffix)) {
            info.put(tag, value);
        }  else if (Rank.equals(tag)) {

        } else if (tag.endsWith(WordClassSuffix)) {
            wordClass = value;
        } else if (tag.endsWith(LevelSuffix)) {

        } else if (tag.endsWith(ExampleSuffix)) {

        }else if (tag.endsWith(DescriptionSuffix)) {

        } else if (tag.contains("_")) {

        } else {
            words.put(tag, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleWordItem)) return false;
        SimpleWordItem p = (SimpleWordItem) o;
        String[] langs = getLangs();
        Arrays.sort(langs);
        return getDisplayName(langs[0]).equals(p.getDisplayName(langs[0]));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        String[] langs = getLangs();
        Arrays.sort(langs);
        return Objects.hash(getDisplayName(langs[0]));
    }

    @Override
    public String toString() {
        String[] langs = getLangs();
        Arrays.sort(langs);
        return getDisplayName(langs[0]);
    }
}
