package com.andreaak.cards.model;

import com.andreaak.common.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.andreaak.cards.utils.FilesHelper.getWordId;

public class LessonItem implements Serializable {

    private String displayName;
    private String fileName;
    private String path;
    private String prefix;
    private File file;
    private boolean sortItems;

    private LanguageItem languageItem;
    private String currentLanguage;
    private ArrayList<WordItem> words = new ArrayList<>();
    private ArrayList<String> ignoredItems = new ArrayList<>();

    public LessonItem(File lessonFile, String prefix, boolean sortItems) {
        this.fileName = lessonFile.getName();
        this.displayName = Utils.getDisplayName(fileName, prefix);
        this.path = lessonFile.getAbsolutePath();
        this.prefix = prefix;
        this.file = lessonFile;
        this.sortItems = sortItems;
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
                continue;
            }

            String wordId = getWordId(wi, getCurrentLanguage());
            if (ignoredItems.contains(wordId))
            {
                it.remove();
            }
        }
        return list;
    }

    public ArrayList<WordItem> getSortedLessonWords() {
        ArrayList<WordItem> list = getLessonWords();

        Collections.sort(list, new WordsComparator(currentLanguage)) ;

        return list;
    }

    public void clear() {

        words.clear();
        ignoredItems.clear();
    }

    public void subClear() {
        WordItem w = words.get(0);
        w.setId(-1);
        words.clear();
        add(w);
    }

    public void add(WordItem word) {
        words.add(word);
    }

    public void addAll(ArrayList<SimpleWordItem> word) {
        words.addAll(word);
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

    public boolean isSortItems() {
        return sortItems;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getOtherLanguage() {
        return currentLanguage.equals(languageItem.getPrimaryLanguage()) ?
                languageItem.getSecondaryLanguage() :
                languageItem.getPrimaryLanguage();
    }

    public boolean isContainsWords() {
        return !words.isEmpty();
    }

    public int wordsCount() {
        return words.size();
    }

    public void ToggleLanguage() {
        currentLanguage = getOtherLanguage();
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

    public void setIgnoredItems(ArrayList<String> list) {
        ignoredItems = list;
    }
}

