package com.andreaak.note.utils.logger;

import android.util.Log;

public class NativeLogger implements ILogger {

    public int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
}
