package com.andreaak.cards.model;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.andreaak.common.utils.Utils;

import java.util.Arrays;
import java.util.List;
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
        String val = getValue(lang);
        if (Utils.isEmpty(val)) {
            return null;
        }
        String wc = getWordClass();
        if (Utils.isEmpty(wc)) {
            return val;
        }
        return (val + " " + wc +
                ("verb".equals(wc) ? (" " + getInfo(lang)) : "")).trim();
    }

    public String getRuDisplayName(String lang1, String lang2) {
        String wc = getWordClass();
        String value = "";
        if (!Utils.isEmpty(wc)) {
            value += (wc + ("verb".equals(wc) ? (" " + getInfo(lang2)) : "") + " ");
        }
        String v1 = getValue(lang1);
        String v2 = getValue(lang2);
        return value + (v2 != null ? v2.trim() : "") + " " + (v1 != null ? v1.trim() : "");
    }

    public String getRuDisplayName(String lang2) {
        return getRuDisplayName(Lang.RU.getCode(), lang2);
    }

    public String getDisplayName(String currentLang, String lang1, String lang2) {
        return Lang.isRu(currentLang) ? getRuDisplayName(lang1, lang2) : getDisplayName(lang2);
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

    private String getPreferredLang() {
        String[] langs = getLangs();
        List<String> langList = Arrays.asList(langs);
        if (langList.contains(Lang.EN.getCode())) {
            return Lang.EN.getCode();
        }
        if (langList.contains(Lang.DE.getCode())) {
            return Lang.DE.getCode();
        }
        if (langs.length > 0) {
            Arrays.sort(langs);
            return langs[0];
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimpleWordItem)) {
            return false;
        }
        SimpleWordItem p = (SimpleWordItem) o;

        String lang1 = getPreferredLang();
        String lang2 = p.getPreferredLang();
        if (lang1 == null || lang2 == null) {
            return false;
        }

        String name1 = getDisplayName(lang1);
        String name2 = p.getDisplayName(lang2);
        return Objects.equals(name1, name2);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        String lang = getPreferredLang();
        if (lang != null) {
            return Objects.hash(getDisplayName(lang));
        }
        return 0;
    }

    @Override
    public String toString() {
        String lang = getPreferredLang();
        if (lang != null) {
            String name = getDisplayName(lang);
            return name != null ? name : "";
        }
        return "";
    }
}
