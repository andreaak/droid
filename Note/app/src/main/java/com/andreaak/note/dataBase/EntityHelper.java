package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.andreaak.note.Constants;
import com.andreaak.note.R;
import com.andreaak.note.utils.ItemType;

import java.util.ArrayList;
import java.util.List;

public class EntityHelper {

    private static final int ROOT = -1;

    private Context context;
    private DataBaseHelper dataBaseHelper;


    public EntityHelper(Context context) {
        this.context = context;
        currentId = ROOT;
    }

    private int currentId;
    public int getCurrentId() {
        return currentId;
    }

    private String currentText;
    public String getCurrentText() {
        return currentText;
    }

    public List<EntityItem> getEntities(int currentId) {

        List<EntityItem> items = dataBaseHelper.GetEntities(currentId);
        if (currentId != ROOT) {
            int parentId = dataBaseHelper.GetParentId(currentId);
            EntityItem item = new EntityItem(parentId, context.getString(R.string.parentDirectory), ItemType.ParentDirectory);
            items.add(0, item);
        }
        this.currentId = currentId;
        return items;
    }

    public List<EntityItem> findNotes(String text) {
        currentText = text;
        return dataBaseHelper.findNotes(text);
    }

    public List<String> getDescriptions(int currentId) {
        return dataBaseHelper.GetDescriptions(currentId);
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
