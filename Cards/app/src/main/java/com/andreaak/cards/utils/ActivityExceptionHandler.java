package com.andreaak.cards.utils;

import android.app.Activity;

import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.logger.Logger;

public class ActivityExceptionHandler implements Thread.UncaughtExceptionHandler {
    public ActivityExceptionHandler(Activity activity) {

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.e(Constants.LOG_TAG, throwable.getMessage(), throwable);
    }
}
