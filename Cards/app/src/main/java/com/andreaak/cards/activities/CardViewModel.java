package com.andreaak.cards.activities;

import androidx.lifecycle.ViewModel;

import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.model.Genus;
import com.andreaak.cards.model.Lang;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.FilesHelper;
import com.andreaak.cards.utils.xml.XmlParser;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class CardViewModel extends ViewModel {

    private CardActivityHelper helper;
    private boolean isStudy;
    private boolean isExample;

    public CardActivityHelper getHelper() {
        return helper;
    }

    public void setHelper(CardActivityHelper helper) {
        this.helper = helper;
    }

    public boolean isStudy() {
        return isStudy;
    }

    public void toggleStudy() {
        this.isStudy = !this.isStudy;
    }

    public boolean isExample() {
        return isExample;
    }

    public void toggleExample() {
        this.isExample = !this.isExample;
    }

    public void initLessonIfNeeded() {
        if (helper != null && helper.lessonItem != null
                && helper.lessonItem.wordsCount() == 1
                && helper.lessonItem.getWords().get(0).getId() == -1) {
            XmlParser.parseLesson(helper.lessonItem);
            String path = FilesHelper.getTempFilePath(helper.lessonItem);
            ArrayList<String> list = FilesHelper.getFileLines(path);
            helper.lessonItem.setIgnoredItems(list);
        }
    }

    public List<String> getLevels() {
        if (helper == null || helper.lessonItem == null) {
            return new ArrayList<>();
        }
        ArrayList<WordItem> words = helper.lessonItem.getLessonWords();
        String language = helper.lessonItem.getCurrentLanguage();
        Set<String> s = new TreeSet<>();

        for (WordItem w : words) {
            if (!Utils.isEmpty(w.getLevel(language))) {
                s.add(w.getLevel(language));
            } else {
                s.add("CC");
            }
        }

        List<String> list = new ArrayList<>(s);
        list.add(0, CardActivity.ALL);
        list.add(CardActivity.A1B2);
        return list;
    }

    public List<String> getQuantitiesForLevel(String level) {
        if (helper == null || helper.lessonItem == null) {
            return new ArrayList<>();
        }
        String language = helper.lessonItem.getCurrentLanguage();
        List<WordItem> wordsForLevel = WordsSpinAdapter.getWordsForLevel(
                helper.lessonItem.getLessonWords(),
                language,
                level
        );

        Set<String> set = new TreeSet<>((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });

        for (WordItem w : wordsForLevel) {
            String q = WordsSpinAdapter.normalizeQuantity(w.getQuantity(language));
            set.add(q);
        }

        List<String> list = new ArrayList<>(set);
        list.add(0, CardActivity.ALL);
        return list;
    }

    public String selectTargetQuantity(List<String> quantities, String preferredQuantity) {
        if (preferredQuantity != null && quantities.contains(preferredQuantity)) {
            return preferredQuantity;
        }
        return CardActivity.ALL;
    }

    public void removeCurrentWord() {
        if (helper != null && helper.lessonItem != null && helper.currentWord != null) {
            String path = FilesHelper.getTempFilePath(helper.lessonItem);
            String wordId = FilesHelper.getWordId(helper.currentWord, helper.lessonItem.getCurrentLanguage());
            FilesHelper.addLineToFile(path, wordId);
        }
    }

    public void clearWords() {
        if (helper != null && helper.lessonItem != null) {
            String path = FilesHelper.getTempFilePath(helper.lessonItem);
            FilesHelper.deleteFile(path);
        }
    }

    public void createFile() {
        XmlParser.createFile();
    }

    public void addItemToFile() {
        if (helper == null || helper.lessonItem == null || helper.lessonItem.getLanguageItem() == null) {
            return;
        }
        String lang1 = helper.lessonItem.getLanguageItem().getPrimaryLanguage();
        String lang2 = helper.lessonItem.getLanguageItem().getSecondaryLanguage();

        if (Lang.isRu(lang1)) {
            XmlParser.addItemToFile(helper.currentWord, lang1, lang2);
        } else {
            XmlParser.addItemToFile(helper.currentWord, lang2, lang1);
        }
    }

    public void openFile(String name) {
        XmlParser.openFile(name);
    }

    public String getCurrentStudyFileName() {
        return Cache.getInstance().getItem(XmlParser.CurrentStudyFileKey);
    }

    private interface WordFieldGetter {
        String get(WordItem item, String lang);
    }

    private String getLocalizedWordField(WordFieldGetter getter) {
        if (helper == null || helper.lessonItem == null || helper.currentWord == null) {
            return "";
        }
        WordItem word = helper.currentWord;
        String info = getter.get(word, helper.lessonItem.getCurrentLanguage());
        if (Utils.isEmpty(info)) {
            info = getter.get(word, helper.lessonItem.getOtherLanguage());
        }
        return info != null ? info : "";
    }

    public String getExample() {
        return getLocalizedWordField(WordItem::getExample);
    }

    public String getDescription() {
        return getLocalizedWordField(WordItem::getDescription);
    }

    public String getWBDescription() {
        return getLocalizedWordField(WordItem::getWBDescription);
    }

    public String getPrap() {
        return getLocalizedWordField(WordItem::getPrap);
    }

    public String getGPTDescription() {
        String gpt = getLocalizedWordField(WordItem::getGPTDescription);
        String wb = getWBDescription();
        String fileName = (helper != null && helper.lessonItem != null) ? helper.lessonItem.getFileName() : "";

        StringBuilder sb = new StringBuilder();
        if (!Utils.isEmpty(gpt)) {
            sb.append(gpt);
        }
        if (!Utils.isEmpty(wb)) {
            if (sb.length() > 0) sb.append("\r\n");
            sb.append(wb);
        }
        if (!Utils.isEmpty(fileName)) {
            if (sb.length() > 0) sb.append("\r\n");
            sb.append(fileName);
        }
        return sb.toString();
    }

    public Queue<String> getSoundFiles(String language) {
        Queue<String> files = new ArrayDeque<>();
        if (helper == null || helper.currentWord == null) {
            return files;
        }

        List<String> words = AppUtils.getWords(helper.currentWord.getValue(language));
        boolean isValid = false;

        for (String word : words) {
            word = word.replace("|", "");
            String fileTemplate = AppUtils.getSoundFile(language, word);
            boolean res = AppUtils.addSoundFile(files, fileTemplate);
            if (res && !Genus.isArticle(word)) {
                isValid = true;
            }
        }

        if (!isValid) {
            files.clear();
        }
        return files;
    }
}
