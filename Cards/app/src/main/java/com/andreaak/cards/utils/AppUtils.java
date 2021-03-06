package com.andreaak.cards.utils;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.WordItem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppUtils {

    public static LessonItem[] getLessons(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith(AppConfigs.getInstance().LessonsPrefix);
            }
        });
        ArrayList<LessonItem> res = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                res.add(new LessonItem(file));
            }
        }

        return res.toArray(new LessonItem[0]);
    }

    public static List<LanguageItem> getLangs(ArrayList<WordItem> words) {

        List<LanguageItem> langItems = new ArrayList<LanguageItem>();

        for (WordItem word: words ) {
            String[] langs = word.getLangs();
            for (int i = 0; i < langs.length - 1; i++) {
                for (int j = i + 1; j < word.getLangs().length; j++) {
                    LanguageItem item = new LanguageItem(langs[i], langs[j]);
                    if(!langItems.contains(item)) {
                        langItems.add(item);
                    }
                    item = new LanguageItem(langs[j], langs[i]);
                    if(!langItems.contains(item)) {
                        langItems.add(item);
                    }
                }
            }
        }

       return langItems;
    }

    public static List<String> getWords(String item) {
        ArrayList<String> result = new ArrayList<String>();
        if (com.andreaak.common.utils.Utils.isEmpty(item)) {
            return result;
        }
        String[] words = item.trim().split(" ");
        boolean isBracket = false;

        for (String word : words) {
            String normalized = word.trim();
            if ("/".equals(normalized)) {
                continue;
            }
            if (normalized.startsWith("(")) {
                isBracket = true;
            }
            if (isBracket) {
                if (normalized.endsWith(")")) {
                    isBracket = false;
                }
                continue;
            }
            result.add(normalized);
        }
        return result;
    }

    public static String getSoundFile(String language, String word, String soundFormat) {

        String region;
        if ("en".equals(language.toLowerCase())) {
            region = "uk";
        } else {
            region = language;
        }

        return AppConfigs.getInstance().SoundsDir + String.format("/%1$s_%5$s/%3$s_%2$s/%4$s_%2$s.%5$s",
                region.toLowerCase(), region.toLowerCase(), word.charAt(0), word, soundFormat);
    }

    public static String getVerbSoundFile(String language, String word, String soundFormat) {

        String region;
        if ("en".equals(language.toLowerCase())) {
            region = "uk";
        } else {
            region = language;
        }

        return AppConfigs.getInstance().SoundsDir + String.format("/Irregular/%1$s/%2$s_%1$s.%3$s",
                region.toLowerCase(), word, soundFormat);
    }
}


