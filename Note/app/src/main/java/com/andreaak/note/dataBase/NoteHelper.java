package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.adapters.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteHelper {
    private Context context;
    private DataBaseHelper myDbHelper;
    private boolean isActive;

    public NoteHelper(Context context, String path, String fileName) {
        this.context = context;
        isActive = OpenDatabase(path, fileName);
    }

    public boolean IsActive() {
        return isActive;
    }

    public List<Item> getDirectory() {

        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();


        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);

        return dir;
    }

    private boolean OpenDatabase(String path, String fileName) {

        boolean res = false;
        if(myDbHelper != null) {
            myDbHelper.close();
        }
        myDbHelper = new DataBaseHelper(context, path, fileName);
        try {
            myDbHelper.openDataBase();
            res  = true;
        } catch (SQLException ex) {
            Log.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        return res;
    }
}
