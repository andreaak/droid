package com.andreaak.cards.utils.logger;

import android.util.Log;

import com.andreaak.cards.configs.Configs;
import com.andreaak.cards.utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileLogger implements ILogger {

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public FileLogger() {
        File log = new File(Configs.LogFile);
        if (log.exists()) {
            log.delete();
        }
        try {
            log.createNewFile();
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public int d(String tag, String msg) {
        String message = getMessage(msg);
        writeMessage(message);
        writeMessage("");
        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        String message = getMessage(msg, tr);
        writeMessage(message);
        //writeMessage("");
        return 0;
    }

    private void writeMessage(String message) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            FileWriter buf = new FileWriter(Configs.LogFile, true);
            buf.append(message);
            buf.flush();
            buf.close();
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
    }

    private String getMessage(String msg, Throwable tr) {
        Calendar c = Calendar.getInstance();
        String formattedDate = df.format(c.getTime());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%1$s Ex: %2$s \nExceeption: %3$s", formattedDate, msg, tr.getClass().toString()) + '\n');
        StackTraceElement[] elements = tr.getStackTrace();
        for(StackTraceElement el : tr.getStackTrace()){
            sb.append(String.format("at %1$s.%2$s (%3$s:%4$d)", el.getClassName(), el.getMethodName(), el.getFileName(), el.getLineNumber()) + '\n');
        }
        return sb.toString();
    }

    private String getMessage(String msg) {
        Calendar c = Calendar.getInstance();
        String formattedDate = df.format(c.getTime());
        return String.format("%1$s Message: %2$s", formattedDate, msg);
    }
}
