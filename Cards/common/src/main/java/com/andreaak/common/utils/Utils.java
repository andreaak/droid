package com.andreaak.common.utils;

import android.content.Context;
import android.widget.Toast;

import com.andreaak.common.utils.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
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
}


