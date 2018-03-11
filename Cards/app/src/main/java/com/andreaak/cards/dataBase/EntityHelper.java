package com.andreaak.cards.dataBase;

import android.content.Context;
import android.database.SQLException;

import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.logger.Logger;
import com.andreaak.cards.R;
import com.andreaak.cards.utils.ItemType;

import java.util.List;

public class EntityHelper {

    private static final int ROOT = -1;

    private Context context;
    private DataBaseHelper dataBaseHelper;
    private int currentId;
    private String currentText;

    public EntityHelper(Context context) {
        this.context = context;
        currentId = ROOT;
    }

    public int getCurrentId() {
        return currentId;
    }

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

    public List<FindNoteItem> findNotes(String text) {
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
