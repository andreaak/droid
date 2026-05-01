package com.andreaak.cards.utils;

import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class FilesHelper {

    public static String getTextFileContent(String path) {
        // This will reference one line at a time
        String line = null;
        StringBuilder sb = new StringBuilder();

        BufferedReader bufferedReader = null;
        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(path), "UTF8");

            // Always wrap FileReader in BufferedReader.
            bufferedReader = new BufferedReader(reader);

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
            // Always close files.
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public static ArrayList<String> getFileLines(String path) {
        // This will reference one line at a time
        String line = null;
        ArrayList<String> list =  new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(path), "UTF8");

            // Always wrap FileReader in BufferedReader.
            bufferedReader = new BufferedReader(reader);

            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
            // Always close files.
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
            }
        }

        return list;
    }

    public static void addLineToFile(String path, String line) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path, true)); // true = append
            writer.write(line);
            writer.newLine(); // перенос строки
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteFile(String path) {

        File file = new File(path);

        if (file.delete()) {
            System.out.println("Файл удалён");
        } else {
            System.out.println("Не удалось удалить файл");
        }
    }

    public static String getTempFilePath(LessonItem lesson) {
        return lesson.getPath() + "temp";
    }

    public static String getWordId(WordItem item, String lang) {
        String word = item.getValue(lang);
        String wordClass = item.getWordClass();
        String wordInfo = item.getInfo(lang);

        return word + wordClass + wordInfo;
    }
}
