package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.andreaak.note.Constants;
import com.andreaak.note.utils.ItemType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String STANDART_DB_PATH = "/data/data/com.andreaak.note/databases/";

    public static String ENTITY = "Entity";
    public static String ID = "ID";
    public static String ENTITY_PARENT_ID = "ParentID";
    public static String ENTITY_ORDER_POSITION = "OrderPosition";
    public static String ENTITY_TYPE = "Type";
    public static String ENTITY_DESCRIPTION = "Description";

    private static String ENTITY_DATA = "EntityData";
    private static String ENTITY_DATA_TEXT = "TextData";
    private static String ENTITY_DATA_HTML = "HtmlData";
    private static String ENTITY_DATA_DATA = "Data";

    private SQLiteDatabase database;

    private String dbPath;

    private static DataBaseHelper instance;

    public static void initInstance(Context context, String dbPath) {
        if (instance != null) {
            instance.close();
        }
        instance = new DataBaseHelper(context, dbPath);
    }

    public static DataBaseHelper getInstance() {
        return instance;
    }

    private DataBaseHelper(Context context, String dbPath) {

        super(context, dbPath, null, 1);
        this.dbPath = dbPath;
    }

    public boolean checkDataBase() {

        boolean res = false;

        SQLiteDatabase checkDB = null;

        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
            checkDB.getVersion();
            res = true;
        } catch (SQLiteException e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return res;
    }

    public void openDataBase() throws SQLException {
        if(database == null || !database.isOpen()) {
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        }
    }

    public List<EntityItem> GetEntities(int parentId) {
        List<EntityItem> items = new ArrayList<EntityItem>();
        Cursor cursor = database.query(ENTITY, new String[]{ID, ENTITY_DESCRIPTION, ENTITY_TYPE},
                ENTITY_PARENT_ID + "=?", new String[]{String.valueOf(parentId)},
                null, null, ENTITY_ORDER_POSITION);
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(DataBaseHelper.ID);
            int id = cursor.getInt(idIndex);

            int descriptionIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION);
            String description = cursor.getString(descriptionIndex);

            int typeIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_TYPE);
            ItemType type = cursor.getInt(typeIndex) == 0 ? ItemType.Directory : ItemType.File;

            EntityItem item = new EntityItem(id, description, type);
            items.add(item);
        }
        cursor.close();
        return items;
    }

    public List<FindNoteItem> findNotes(String text) {

        List<FindNoteItem> items = new ArrayList<FindNoteItem>();

        String sql = "SELECT Entity.id, Entity.description " +
                    "FROM Entity, EntityData " +
                    "WHERE Entity.id=EntityData.id AND EntityData.TextData LIKE ? ESCAPE '#'";
        Cursor cursor = database.rawQuery(sql, new String[]{"%" + text + "%"});
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(DataBaseHelper.ID);
            int id = cursor.getInt(idIndex);

            int descriptionIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION);
            String description = cursor.getString(descriptionIndex);

            FindNoteItem item = new FindNoteItem(id, description, ItemType.File,
                    Constants.getText("/", GetDescriptions(id)));
            items.add(item);
        }
        cursor.close();
        return items;
    }


    public List<String> GetDescriptions(int currentId) {

        List<String> items = new ArrayList<String>();

        while (true) {

            Cursor cursor = database.query(ENTITY, new String[]{ENTITY_DESCRIPTION, ENTITY_PARENT_ID},
                    ID + "=?", new String[]{String.valueOf(currentId)},
                    null, null, ENTITY_ORDER_POSITION);

            if (cursor.moveToNext()) {

                int descriptionIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION);
                String description = cursor.getString(descriptionIndex);

                int parentIdIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID);
                currentId = cursor.getInt(parentIdIndex);

                items.add(description);
                cursor.close();
            } else {
                cursor.close();
                break;
            }
        }
        Collections.reverse(items);
        return items;
    }

    public String GetEntityDataHtml(int id) {
        Cursor cursor = database.query(ENTITY_DATA, new String[]{ENTITY_DATA_HTML},
                ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        String text = "";
        if (cursor.moveToNext()) {
            int textIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DATA_HTML);
            text = cursor.getString(textIndex);
        }

        cursor.close();
        return text;
    }

    public int GetParentId(int id) {
        Cursor cursor = database.query(ENTITY, new String[]{ENTITY_PARENT_ID},
                ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        int parentId = 0;
        if (cursor.moveToNext()) {
            int parentIdIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID);
            parentId = cursor.getInt(parentIdIndex);
        }

        cursor.close();
        return parentId;
    }

    @Override
    public synchronized void close() {

        if (database != null)
            database.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
