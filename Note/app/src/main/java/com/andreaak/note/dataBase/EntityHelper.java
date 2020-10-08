package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.SQLException;

import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.note.R;

import java.util.List;

public class EntityHelper {

    public static final int ROOT = -1;

    private Context context;
    private DataBaseHelper dataBaseHelper;
    private int currentId;
    private String currentText;

    public EntityHelper(Context context) {
        this.context = context;
        currentId = ROOT;

        dataBaseHelper = DataBaseHelper.getInstance();
    }

    public int getCurrentId() {
        return currentId;
    }

    public void setCurrentId(int currentId) {
        this.currentId = currentId;
    }

    public String getCurrentText() {
        return currentText;
    }

    public List<EntityItem> getChildEntities(int currentId) {

        List<EntityItem> items = dataBaseHelper.getChildEntities(currentId);
        if (currentId != ROOT) {
            int parentId = dataBaseHelper.getParentId(currentId);
            EntityItem item = new EntityItem(parentId, context.getString(R.string.parentDirectory), ItemType.ParentDirectory, 0);
            items.add(0, item);
        }
        this.currentId = currentId;
        return items;
    }

    public List<FindNoteItem> findNotes(String text) {
        currentText = text;
        return dataBaseHelper.findNotes(text);
    }

    public List<String> getDescriptions(int currentId) {
        return dataBaseHelper.getDescriptions(currentId);
    }

    public EntityItem getNextEntity(int currentId) {
        return dataBaseHelper.getNextEntity(currentId);
    }

    public EntityItem getPreviousEntity(int currentId) {
        return dataBaseHelper.getPreviousEntity(currentId);
    }

    public String getEntityDataHtml(int currentId) {
        return dataBaseHelper.getEntityDataHtml(currentId);
    }

    public boolean openDatabase() {

        boolean res = false;
        try {
            dataBaseHelper.openDataBase();
            res = true;
        } catch (SQLException ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        return res;
    }

    public void close() {
        if (dataBaseHelper != null) {
            dataBaseHelper.close();
        }
    }
}
