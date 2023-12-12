package com.andreaak.cards.utils;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.VerbFormType;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

public class AppUtils {

    private static final String[] SOUND_FORMATS = {"mp3", "wav"};

    private static final ReplaceItem[] NORMALIZATION = {new ReplaceItem("ä", "!a") ,
            new ReplaceItem("ö", "!o"),
            new ReplaceItem("ü", "!u")};

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

    public static ArrayList<VerbForm> getVerbForms(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return true;
            }
        });
        ArrayList<VerbForm> res = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, new SortByFileName());
            for (File file : files) {
                res.add(new VerbForm(file));
            }
        }

        return res;
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

    public static List<VerbFormType> getVerbFormTypes(VerbFormItem[] words) {

        ArrayList<VerbFormType> items = new ArrayList<>();

        for (VerbFormItem word: words) {
            items.add(word.FormType);
        }

        return items;
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

    public static boolean addSoundFile(Queue<String> files, String fileTemplate) {

        for (String soundFormat : SOUND_FORMATS) {
            String filePath = fileTemplate + soundFormat;
            File file = new File(filePath);
            if (file.exists()) {
                files.add(filePath);
                return true;
            }
        }
        return false;
    }

    public static String getSoundFile(String language, String word) {

        SoundFileData data = getSoundFileData(language);

        word = Normalize(word);
        return AppConfigs.getInstance().SoundsDir + String.format("/%1$s/%3$s%2$s/%4$s%2$s.",
                data.Region.toLowerCase(), data.Suffix,
                word.startsWith("!") ? word.substring(0, 1) : word.charAt(0), word);
    }

    public static String getVerbSoundFile(String language, String word) {

        SoundFileData data = getSoundFileData(language);

        word = Normalize(word);

        return AppConfigs.getInstance().SoundsDir + String.format("/Irregular/%1$s/%3$s%2$s.",
                data.Region.toLowerCase(), data.Suffix, word);
    }

    private static SoundFileData getSoundFileData(String language) {
        String region;
        String suffix;
        if ("en".equals(language.toLowerCase())) {
            region = "uk";
            suffix = "_uk";
        } else {
            region = language;
            suffix = "";
        }
        return new SoundFileData(region, suffix);
    }


    private static String Normalize(String word) {
        word = word.toLowerCase();
        for (ReplaceItem symbol : NORMALIZATION) {
            if(word.contains(symbol.Source)) {
                word = word.replaceAll(symbol.Source, symbol.Dest);
            }
        }
        return word;
    }
}

class SoundFileData {
    public String Region;
    public String Suffix;

    public SoundFileData(String region, String suffix) {
        Region = region;
        Suffix = suffix;
    }
}

class ReplaceItem {
    public String Source;
    public String Dest;


    public ReplaceItem(String source, String dest) {
        Source = source;
        Dest = dest;
    }
}

class SortByFileName implements Comparator<File> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(File a, File b)
    {
        return Utils.normalize(a.getName()).compareTo(Utils.normalize(b.getName()));
    }
}


