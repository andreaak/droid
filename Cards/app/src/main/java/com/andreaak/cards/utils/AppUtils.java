package com.andreaak.cards.utils;

import android.graphics.Paint;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.Lang;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.VerbFormType;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.xml.XmlParser;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class AppUtils {

    public static String combineText(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!Utils.isEmpty(part)) {
                if (sb.length() > 0) {
                    sb.append("  ");
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private static final String[] SOUND_FORMATS = {"mp3", "wav"};

    private static final ReplaceItem[] NORMALIZATION = {new ReplaceItem("ä", "!a") ,
            new ReplaceItem("ö", "!o"),
            new ReplaceItem("ü", "!u")};

    public static void adoptFontSize(TextView textView, String text, float maxSp, float minSp) {

        if(Utils.isEmpty(text)) {
            return;
        }

        int availableWidth = textView.getWidth()
                - textView.getPaddingLeft() - textView.getPaddingRight();
        if (availableWidth <= 0) {
            textView.post(() -> adoptFontSize(textView, text, maxSp, minSp));
            return;
        }

        float maxPx = toPx(textView, maxSp);
        float minPx = toPx(textView, minSp);

        // Копия Paint сохраняет шрифт и стиль TextView.
        Paint paint = new Paint(textView.getPaint());
        paint.setTextSize(maxPx);

        float widestLine = 0f;
        for (String line : text.split("\n")) {
            widestLine = Math.max(widestLine, paint.measureText(line));
        }

        widestLine += 20;

        float sizePx = maxPx;
        boolean fits = widestLine <= availableWidth;
        if (!fits) {
            sizePx = Math.max(minPx, maxPx * availableWidth / widestLine);
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx);
        // Влезло — по центру; уменьшали — выравниваем по левому краю, как раньше.
        textView.setGravity(fits ? Gravity.CENTER : Gravity.START);
    }

    private static float toPx(TextView textView, float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, textView.getResources().getDisplayMetrics());
    }

    public static void setViewTextSize(TextView textView, float size) {
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Math.round(0), Math.round(0), Math.round(0), Math.round(0));

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setLayoutParams(params);
    }

    public static void setViewGravity(TextView textView, boolean isCenter) {
        if(isCenter){
            textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        } else {
            textView.setGravity(Gravity.LEFT);
        }
        textView.setPadding(0, 0, 0, 0);
    }

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
                    return (Utils.isEmpty(prefix) || s.startsWith(prefix) || s.startsWith("!" + prefix))
                            && s.endsWith(AppConfigs.getInstance().LessonsExtension);
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

    public static ArrayList<SimpleWordItem> getSimpleWortItems(String paths, String prefixes) {

        String[] ps = paths.split("\\|");
        String[] px = prefixes.split("\\|");

        ArrayList<DirectoryItem> res = new ArrayList<>();

        for (int i = 0; i < ps.length; i++) {
            res.add(new DirectoryItem(ps[i], px[i]));
        }
        return getSimpleWortItems(res);
    }

    private static ArrayList<SimpleWordItem> getSimpleWortItems(ArrayList<DirectoryItem> paths) {

        ArrayList<SimpleWordItem> res = new ArrayList<>();
        for(DirectoryItem di : paths) {

            ArrayList<SimpleWordItem> allItems = Cache.getInstance().getWordItems(di.path);

            if(allItems != null){
                res.addAll(allItems);
                continue;
            }

            ArrayList<SimpleWordItem> res1 = new ArrayList<>();
            File directory = new File(di.path);
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return isProperFile(s);
                }
            });

            if (files != null) {
                for (File file : files) {
                    String path = file.getPath().toLowerCase();

                    if(path.contains("_all")
                    || (!path.contains("no_preffix") && path.contains("_preffix"))) {
                        continue;
                    }
                    res1.addAll(XmlParser.getSimpleWordItems(file.getPath()));
                }
            }

            res1 = res1.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

            Cache.getInstance().add(di.path, new ArrayList<>(res1));
            res.addAll(res1);
        }

        return new ArrayList<>(res);
    }

    private static boolean isProperFile(String file) {
        return !file.startsWith("!") && file.endsWith(AppConfigs.getInstance().LessonsExtension);
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
        if (Lang.EN.getCode().equalsIgnoreCase(language)) {
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


