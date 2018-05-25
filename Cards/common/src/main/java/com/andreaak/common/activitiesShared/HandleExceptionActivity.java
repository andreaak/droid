package com.andreaak.common.activitiesShared;

import android.app.Activity;
import android.os.Bundle;

import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

public class HandleExceptionActivity extends Activity {

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            androidDefaultUEH.uncaughtException(thread, e);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}
