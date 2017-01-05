package com.andreaak.note.adapters;

public class Constants {
    public static final String FILE_ICON = "file_icon";
    public static final String DIRECTORY_ICON = "directory_icon";
    public static final String DIRECTORY_UP = "directory_up";
    public static final String DRAWABLE = "drawable/";
    public static final String LOG_TAG = "Note";

    public static String getText(String ... args) {

        StringBuffer sb = new StringBuffer();
        for(String str : args) {
            if(sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(str);
        }
        return sb.toString();
    }
}
