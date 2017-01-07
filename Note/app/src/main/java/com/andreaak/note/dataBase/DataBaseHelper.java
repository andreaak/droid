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
        if(instance != null) {
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
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
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
        database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    public List<NoteItem> GetEntities(int parentId) {
        List<NoteItem> items = new ArrayList<NoteItem>();
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

            NoteItem item = new NoteItem(id, description, type);
            items.add(item);
        }
        cursor.close();
        return items;
    }

    public String GetEntityData(int id) {
        Cursor cursor = database.query(ENTITY_DATA, new String[]{ENTITY_DATA_TEXT}, ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        String text = "";
        if (cursor.moveToNext()) {
            int textIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DATA_TEXT);
            text = cursor.getString(textIndex);
        }

        cursor.close();
        return text;
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
