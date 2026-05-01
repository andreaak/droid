package com.andreaak.cards.utils;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class AppUtils {

    private static final String[] SOUND_FORMATS = {"mp3", "wav"};

    private static final ReplaceItem[] NORMALIZATION = {new ReplaceItem("ä", "!a") ,
            new ReplaceItem("ö", "!o"),
            new ReplaceItem("ü", "!u")};

    public static ArrayList<LessonItem> getLessons(String path, final String prefix, boolean sortItems) {

        if(path == null || path.trim().length() == 0) {
            return null;
        }
        ArrayList<LessonItem> res = new ArrayList<>();
        try {
            File directory = new File(path);
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.startsWith(prefix) && s.endsWith(AppConfigs.getInstance().LessonsExtension);
                }
            });

            if (files != null) {
                Arrays.sort(files);
                for (File file : files) {
                    res.add(new LessonItem(file, prefix, sortItems));
                }
            }

        } catch (Exception ex) {
            String message = ex.getMessage();
            return res;
        }
        return res;
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

    public static ArrayList<SimpleWordItem> getSimpleWortItems(String paths, String prefixes, LanguageItem lg) {

        String[] ps = paths.split("\\|");
        String[] px = prefixes.split("\\|");

        ArrayList<DirectoryItem> res = new ArrayList<>();

        for (int i = 0; i < ps.length; i++) {
            res.add(new DirectoryItem(ps[i], px[i]));
        }
        return getSimpleWortItems(res, lg);
    }

    private static HashMap<String, ArrayList<SimpleWordItem>> items = new HashMap<>();



    private static ArrayList<SimpleWordItem> getSimpleWortItems(ArrayList<DirectoryItem> paths, LanguageItem lg) {

        ArrayList<SimpleWordItem> res = new ArrayList<>();
        for(DirectoryItem di : paths) {
            ArrayList<SimpleWordItem> allItems = items.get(di.path);

            if(allItems != null){
                res.addAll(allItems);
                continue;
            }

            File directory = new File(di.path);
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return true;
                }
            });

            if (files != null) {
                for (File file : files) {
                    String path = file.getPath().toLowerCase();

                    if(path.contains("_all")
                    || (!path.contains("no_preffix") && path.contains("_preffix"))) {
                        continue;
                    }
                    res.addAll(XmlParser.getSimpleWordItems(file.getPath()));
                }
            }
            items.put(di.path, new ArrayList<>(res));
        }

        return new ArrayList<>(res);
    }

    public static List<LanguageItem> getLangs(ArrayList<WordItem> words) {

        List<LanguageItem> langItems = new ArrayList<LanguageItem>();

        for (WordItem word: words ) {
            String[] langs = word.getLangs();
            Arrays.sort(langs);

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
        return AppConfigs.getInstance().SoundsDir + String.format("/%1$s/%2$s/%3$s.",
                data.Region.toLowerCase(),
                word.startsWith("!") ? word.substring(0, 2) : word.charAt(0),
                word);
    }

    public static String getVerbSoundFile(String language, String word) {

        SoundFileData data = getSoundFileData(language);

        word = Normalize(word);

        return AppConfigs.getInstance().SoundsDir + String.format("/Irregular/%1$s/%2$s.",
                data.Region.toLowerCase(), word);
    }

    private static SoundFileData getSoundFileData(String language) {
        String region;
        String suffix;
        if ("en".equals(language.toLowerCase())) {
            region = "us";
            suffix = "";
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
    //public String Suffix;

    public SoundFileData(String region, String suffix) {
        Region = region;
        //Suffix = suffix;
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


