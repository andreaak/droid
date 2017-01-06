package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.andreaak.note.Constants;
import com.andreaak.note.R;
import com.andreaak.note.utils.ItemType;

import java.util.List;

public class NoteHelper {

    private static final int ROOT = -1;

    private Context context;
    private DataBaseHelper dataBaseHelper;
    private int currentId;

    public NoteHelper(Context context) {
        this.context = context;
        currentId = ROOT;
    }

    public int getCurrentId() {
        return currentId;
    }

    public List<NoteItem> getNoteItems(int currentId) {

        List<NoteItem> items = dataBaseHelper.GetEntities(currentId);
        if (currentId != ROOT) {
            int parentId = dataBaseHelper.GetParentId(currentId);
            NoteItem item = new NoteItem(parentId, context.getString(R.string.parentDirectory), ItemType.ParentDirectory);
            items.add(0, item);
        }
        this.currentId = currentId;
        return items;
    }

    public boolean openDatabase() {

        boolean res = false;
        if (dataBaseHelper != null) {
            dataBaseHelper.close();
        }
        dataBaseHelper = DataBaseHelper.getInstance();
        try {
            dataBaseHelper.openDataBase();
            res = true;
        } catch (SQLException ex) {
            Log.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        return res;
    }

    public void close() {
        if (dataBaseHelper != null) {
            dataBaseHelper.close();
        }
    }
}
