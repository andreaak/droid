package com.andreaak.note.utils;

import android.content.Context;
import android.widget.Toast;

import com.andreaak.note.utils.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class Utils {

    private static final String TITL_FMT = "yyMMdd-HHmmss";

    public static Context acx;

    private Utils() {
    }

    public static void init(Context ctx) {
        acx = ctx.getApplicationContext();
    }

    public static String getSeparatedText(String sep, List<String> args) {

        StringBuffer sb = new StringBuffer();
        for (String str : args) {
            if (sb.length() != 0) {
                sb.append(sep);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    public static void showText(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }

    public static void showText(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

//    private static File cchFile(String flNm) {
//        File cche = Utils.acx.getExternalCacheDir();
//        return (cche == null || flNm == null) ? null : new File(cche.getPath() + File.separator + flNm);
//    }

//    static File str2File(String str, String name) {
//        if (str == null) return null;
//        byte[] buf = str.getBytes();
//        File fl = cchFile(name);
//        if (fl == null) return null;
//        BufferedOutputStream bs = null;
//        try {
//            bs = new BufferedOutputStream(new FileOutputStream(fl));
//            bs.write(buf);
//        } catch (Exception e) {
//            Log.e(Constants.LOG_TAG, e.getMessage(), e);
//        } finally {
//            if (bs != null) try {
//                bs.close();
//            } catch (Exception e) {
//                Log.e(Constants.LOG_TAG, e.getMessage(), e);
//            }
//        }
//        return fl;
//    }

    public static boolean saveToFile(InputStream is, File file) {

        BufferedInputStream bufferedStream = null;
        if (is != null) try {

            FileOutputStream fileOutput = new FileOutputStream(file);
            bufferedStream = new BufferedInputStream(is);
            byte[] buffer = new byte[4096];
            int bufferLength = 0;

            while ((bufferLength = bufferedStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            return true;
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            try {
                if (bufferedStream != null) bufferedStream.close();
            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

//    static String time2Titl(Long milis) {       // time -> yymmdd-hhmmss
//        Date dt = (milis == null) ? new Date() : (milis >= 0) ? new Date(milis) : null;
//        return (dt == null) ? null : new SimpleDateFormat(TITL_FMT, Locale.US).format(dt);
//    }
//
//    static String titl2Month(String titl) {
//        return titl == null ? null : ("20" + titl.substring(0, 2) + "-" + titl.substring(2, 4));
//    }
}


