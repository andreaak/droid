package com.andreaak.common.utils;

import android.content.Context;
import android.widget.Toast;

import com.andreaak.common.utils.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    private static final String TITL_FMT = "yyMMdd-HHmmss";
    private final static String BETWEEN_LOWER_AND_UPPER = "(?<=\\p{Ll})(?=\\p{Lu})";
    private final static String BEFORE_UPPER_AND_LOWER = "(?<=\\p{L})(?=\\p{Lu}\\p{Ll})";

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
        if (!checkOrCreateFolderForFile(file)) {
            return false;
        }

        BufferedInputStream bufferedStream = null;
        if (is != null) try {

            FileOutputStream fileOutput = new FileOutputStream(file);
            bufferedStream = new BufferedInputStream(is);
            byte[] buffer = new byte[4096];
            int bufferLength = 0;

            while ((bufferLength = bufferedStream. read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            return true;
        } catch (Exception ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedStream != null) bufferedStream.close();
            } catch (Exception ex) {
                Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static String getFileNameWithoutExtensions(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    static Pattern SPLIT_CAMEL_CASE = Pattern.compile(
            BETWEEN_LOWER_AND_UPPER +"|"+ BEFORE_UPPER_AND_LOWER + "|" + "_"
    );

    public static String[] splitCamelCaseString(String s) {
        return SPLIT_CAMEL_CASE.split(s);
    }

    public static String getDisplayName(String fileName, String prefix) {
        String[] res =
                Utils.splitCamelCaseString(
                        Utils.getFileNameWithoutExtensions(fileName)
                                .replace(prefix, ""));

        StringBuilder sb = new StringBuilder();
        for (String str : res) {
            if(sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(str.substring(0, 1).toUpperCase() + str.substring(1));
        }
        return sb.toString();
    }

    public static boolean checkOrCreateFolderForFile(File file) {
        File folder = file.getParentFile();
        return checkOrCreateFolder_(folder);
    }

    private static boolean checkOrCreateFolder_(File folder) {
        if(folder.exists()) {
            return true;
        }

        File parent = folder.getParentFile();
        if(!parent.exists()) {
            checkOrCreateFolder_(parent);
        }

        try {
            return folder.mkdir();
        } catch (Exception ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
            return false;
        }
    }
}


