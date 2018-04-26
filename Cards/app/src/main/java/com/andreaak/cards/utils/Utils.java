package com.andreaak.cards.utils;

import android.content.Context;
import android.widget.Toast;

import com.andreaak.cards.configs.Configs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private static final String TITL_FMT = "yyMMdd-HHmmss";

    public static Context acx;

    private Utils() {
    }

    public static void init(Context ctx) {
        acx = ctx.getApplicationContext();
    }

    public static String getSeparatedText(String sep, List<String> args) {

        StringBuffer sb = new StringBuffer();
        for (String str : args) {
            if (sb.length() != 0) {
                sb.append(sep);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    public static void showText(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    public static void showText(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean saveToFile(InputStream is, File file) {

        BufferedInputStream bufferedStream = null;
        if (is != null) try {

            FileOutputStream fileOutput = new FileOutputStream(file);
            bufferedStream = new BufferedInputStream(is);
            byte[] buffer = new byte[4096];
            int bufferLength = 0;

            while ((bufferLength = bufferedStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            return true;
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            try {
                if (bufferedStream != null) bufferedStream.close();
            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    public static String getFileNameWithoutExtensions(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    public static File[] getLessons(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });
        if (files == null) {
            files = new File[0];
        }
        Arrays.sort(files);
        return files;
    }

    public static List<LanguageItem> getLangs(ArrayList<WordItem> words) {

        List<LanguageItem> langItems = new ArrayList<LanguageItem>();
        WordItem word = words.get(words.size() - 1);

        String[] langs = word.getLangs();
        for (int i = 0; i < langs.length - 1; i++) {
            for (int j = i + 1; j < word.getLangs().length; j++) {
                langItems.add(new LanguageItem(langs[i], langs[j]));
                langItems.add(new LanguageItem(langs[j], langs[i]));
            }
        }

        return langItems;
    }

    public static List<String> getWords(String item) {
        ArrayList<String> result = new ArrayList<String>();
        if(isEmpty(item)) {
            return result;
        }
        String[] words = item.trim().split(" ");
        boolean isBracket = false;

        for(String word : words) {
            String normalized = word.trim();
            if("/".equals(normalized)) {
                continue;
            }
            if(normalized.startsWith("(")) {
                isBracket = true;
            }
            if(isBracket) {
                if(normalized.endsWith(")")) {
                    isBracket = false;
                }
                continue;
            }
            result.add(normalized);
        }
        return result;
    }

    public static String getSoundFile(String language, String word) {

        String region;
        if("en".equals(language.toLowerCase())) {
            region = "uk";
        } else {
            region = language;
        }

        return Configs.SoundsDir + String.format("/%1$s/%2$s_%3$s/%4$s_%2$s.mp3",
                region.toUpperCase(), region.toLowerCase(), word.charAt(0), word);
    }

    public static String getVerbSoundFile(String language, String word) {

        String region;
        if("en".equals(language.toLowerCase())) {
            region = "uk";
        } else {
            region = language;
        }

        return Configs.SoundsDir + String.format("/Irregular/%1$s/%2$s_%1$s.mp3",
                region.toLowerCase(), word);
    }
}


