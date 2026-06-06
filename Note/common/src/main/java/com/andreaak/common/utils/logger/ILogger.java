package com.andreaak.common.utils.logger;

public interface ILogger {

    int d(String tag, String msg);

    int e(String tag, String msg, Throwable tr);
}
