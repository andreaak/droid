package com.andreaak.note.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UT {

    private static final String L_TAG = "_X_";

    static final String MYROOT = "DEMORoot";
    static final String MIME_TEXT = "text/plain";
    static final String MIME_FLDR = "application/vnd.google-apps.folder";

    static final String TITL = "titl";
    static final String GDID = "gdid";
    static final String MIME = "mime";

    private static final String TITL_FMT = "yyMMdd-HHmmss";

    static Context acx;

    public static void init(Context ctx) {
        acx = ctx.getApplicationContext();
    }

    private UT() {
    }

    static ContentValues newCVs(String titl, String gdId, String mime) {
        ContentValues cv = new ContentValues();
        if (titl != null) cv.put(TITL, titl);
        if (gdId != null) cv.put(GDID, gdId);
        if (mime != null) cv.put(MIME, mime);
        return cv;
    }

    private static File cchFile(String flNm) {
        File cche = UT.acx.getExternalCacheDir();
        return (cche == null || flNm == null) ? null : new File(cche.getPath() + File.separator + flNm);
    }

    static File str2File(String str, String name) {
        if (str == null) return null;
        byte[] buf = str.getBytes();
        File fl = cchFile(name);
        if (fl == null) return null;
        BufferedOutputStream bs = null;
        try {
            bs = new BufferedOutputStream(new FileOutputStream(fl));
            bs.write(buf);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            if (bs != null) try {
                bs.close();
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
        return fl;
    }

    static boolean saveToFile(InputStream is, File file) {

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
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            try {
                if (bufferedStream != null) bufferedStream.close();
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    static String time2Titl(Long milis) {       // time -> yymmdd-hhmmss
        Date dt = (milis == null) ? new Date() : (milis >= 0) ? new Date(milis) : null;
        return (dt == null) ? null : new SimpleDateFormat(TITL_FMT, Locale.US).format(dt);
    }

    static String titl2Month(String titl) {
        return titl == null ? null : ("20" + titl.substring(0, 2) + "-" + titl.substring(2, 4));
    }
}


