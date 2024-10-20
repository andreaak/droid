package com.andreaak.cards.model;

import android.text.Html;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LessonItem implements Serializable {

    private String displayName;
    private String fileName;
    private String path;
    private String prefix;
    private File file;

    private LanguageItem languageItem;
    private String currentLanguage;
    private ArrayList<WordItem> words = new ArrayList<>();

    public LessonItem(File lessonFile, String prefix) {
        this.fileName = lessonFile.getName();
        this.displayName = Utils.getDisplayName(fileName, prefix);
        this.path = lessonFile.getAbsolutePath();
        this.prefix = prefix;
        this.file = lessonFile;
    }

    public ArrayList<WordItem> getWords() {
        return words;
    }

    public ArrayList<WordItem> getLessonWords() {
        ArrayList<WordItem> list = new ArrayList<WordItem>(words);

        for (Iterator<WordItem> it = list.iterator(); it.hasNext();) {
            WordItem wi = it.next();

            List<String> langs = Arrays.asList(wi.getLangs());

            if (!langs.contains(languageItem.getPrimaryLanguage())
                || !langs.contains(languageItem.getSecondaryLanguage()))
            {
                it.remove();
            }
        }
        return list;
    }

    public ArrayList<WordItem> getSortedLessonWords() {
        ArrayList<WordItem> list = getLessonWords();

        Collections.sort(list, new Comparator<WordItem>() {
            @Override
            public int compare(WordItem word1, WordItem word2)
            {
                String value1 =  normalize(word1.getValue(currentLanguage));
                String value2 =  normalize(word2.getValue(currentLanguage));
                return  value1.compareTo(value2);
            }

            private String normalize(String value) {

                value = Html.fromHtml(value).toString();

                return value.replace("der ", "")
                        .replace("die ", "")
                        .replace("das ", "")
                        .replace("der(die) ", "")
                        .replace("die(der) ", "")
                        .replace("der(das) ", "");
            }
        });
        return list;
    }

    public void clear() {
        words.clear();
    }

    public void add(WordItem word) {
        words.add(word);
    }

    public LanguageItem getLanguageItem() {
        return languageItem;
    }

    public void setLanguageItem(LanguageItem languageItem) {
        this.languageItem = languageItem;
        currentLanguage = languageItem.getPrimaryLanguage();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isContainsWords() {
        return !words.isEmpty();
    }

    public void ToggleLanguage() {
        currentLanguage = currentLanguage.equals(languageItem.getPrimaryLanguage()) ?
                languageItem.getSecondaryLanguage() :
                languageItem.getPrimaryLanguage();
    }

    public void resetLanguage() {
        currentLanguage = languageItem.getPrimaryLanguage();
    }

    public void changeWord(WordItem word) {

        for (int i = 0; i < words.size(); i++) {
            WordItem existWord = words.get(i);
            if (existWord.getId() == word.getId()) {
                words.set(i, word);
                break;
            }
        }
    }

    public File getFile() {
        return file;
    }
}
