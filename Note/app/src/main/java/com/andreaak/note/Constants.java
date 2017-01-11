package com.andreaak.note;

import java.util.List;

public class Constants {
    public static final String LOG_TAG = "Note";

    public static String getText(String sep, List<String> args) {

        StringBuffer sb = new StringBuffer();
        for (String str : args) {
            if (sb.length() != 0) {
                sb.append(sep);
            }
            sb.append(str);
        }
        return sb.toString();
    }
}
