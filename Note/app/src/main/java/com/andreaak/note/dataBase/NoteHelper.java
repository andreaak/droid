package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.andreaak.note.Constants;
import com.andreaak.note.utils.ItemType;

import java.util.ArrayList;
import java.util.List;

public class NoteHelper {

    public static final int ROOT = -1;

    private Context context;
    private DataBaseHelper dataBaseHelper;
    private boolean isActive;
    private int parentId;
    private int parentDescription;

    public NoteHelper(Context context, String path, String fileName) {
        this.context = context;
        isActive = OpenDatabase(path, fileName);
    }

    public boolean IsActive() {
        return isActive;
    }

    public List<NoteItem> getNoteItems(int currentId) {

        List<NoteItem> items = new ArrayList<NoteItem>();
        Cursor cursor = dataBaseHelper.GetEntities(currentId);
        while(cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(DataBaseHelper.ID);
            int id = cursor.getInt(idIndex);

            int parentIdIndex = cursor.getColumnIndex(DataBaseHelper.PARENT_ID);
            int parentId_ = cursor.getInt(parentIdIndex);

            int descriptionIndex = cursor.getColumnIndex(DataBaseHelper.DESCRIPTION);
            String description = cursor.getString(descriptionIndex);

            int typeIndex = cursor.getColumnIndex(DataBaseHelper.TYPE);
            ItemType type = cursor.getInt(typeIndex) == 0 ? ItemType.Directory : ItemType.File;

            NoteItem item = new NoteItem(id, parentId_, description, "", type);
            items.add(item);
        }

        cursor.close();
        if(currentId != ROOT) {
            NoteItem item = new NoteItem(parentId, parentId, "..", "", ItemType.ParentDirectory);
            items.add(0, item);
        }
        parentId = currentId;
        return items;
    }

    private boolean OpenDatabase(String path, String fileName) {

        boolean res = false;
        if(dataBaseHelper != null) {
            dataBaseHelper.close();
        }
        dataBaseHelper = new DataBaseHelper(context, path, fileName);
        try {
            dataBaseHelper.openDataBase();
            res  = true;
        } catch (SQLException ex) {
            Log.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        return res;
    }
}
