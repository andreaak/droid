package com.andreaak.cards.utils;

import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            // Always close files.
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                }
            }
        }

        return sb.toString();
    }
}
