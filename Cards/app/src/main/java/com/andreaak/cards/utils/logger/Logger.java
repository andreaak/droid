package com.andreaak.cards.utils.logger;

public class Logger {

    private static ILogger logger = new NativeLogger();

    public static void setLogger(ILogger lg) {
        logger = lg;
    }

    public static int d(String tag, String msg) {
        return logger.d(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return logger.e(tag, msg, tr);
    }
}
