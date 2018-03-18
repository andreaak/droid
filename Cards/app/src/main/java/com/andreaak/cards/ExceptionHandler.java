package com.andreaak.cards;

import android.app.Activity;

import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.logger.Logger;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    public ExceptionHandler(Activity activity) {

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.e(Constants.LOG_TAG, throwable.getMessage(), throwable);
    }
}
