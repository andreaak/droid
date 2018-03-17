package com.andreaak.cards;

import android.app.Application;

import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.logger.Logger;

public class CardsApplication extends Application {

    private Thread.UncaughtExceptionHandler androidDefaultUEH;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            androidDefaultUEH.uncaughtException(thread, e);
        }
    };

    public void onCreate ()
    {
        super.onCreate();
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}
