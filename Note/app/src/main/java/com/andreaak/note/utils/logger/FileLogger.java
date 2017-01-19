package com.andreaak.note.utils.logger;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.andreaak.note.utils.Configs.LOG_FILE;
import static com.andreaak.note.utils.Constants.LOG_TAG;

public class FileLogger implements ILogger {

    public FileLogger() {
        File log = new File(LOG_FILE);
        if (log.exists()) {
            log.delete();
        }
        try {
            log.createNewFile();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public int d(String tag, String msg) {
        String message = getMessage(tag, msg);
        writeMessage(message);
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        String message = getMessage(tag, msg, tr);
        writeMessage(message);
        return 0;
    }

    private void writeMessage(String message) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(LOG_FILE, true));
            buf.append(message);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private String getMessage(String tag, String msg, Throwable tr) {
        return String.format("Tag: %1$s Ex: %2$s St: %3$s", tag, msg, tr.toString());
    }

    private String getMessage(String tag, String msg) {
        return String.format("Tag: %1$s Ex: %2$s", tag, msg);
    }
}
