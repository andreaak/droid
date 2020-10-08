package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.note.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.andreaak.common.utils.Utils.showText;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static String ENTITY = "Entity";
    public static String ID = "ID";
    public static String ENTITY_PARENT_ID = "ParentID";
    public static String ENTITY_ORDER_POSITION = "OrderPosition";
    public static String ENTITY_TYPE = "Type";
    public static String ENTITY_DESCRIPTION = "Description";
    //The Android's default system path of your application database.
    private static String ENTITY_DATA = "EntityData";
    private static String ENTITY_DATA_TEXT = "TextData";
    private static String ENTITY_DATA_HTML = "HtmlData";
    private static String ENTITY_DATA_DATA = "Data";

    private static DataBaseHelper instance;

    private SQLiteDatabase database;
    private String dbPath;
    private Context context;

    private DataBaseHelper(Context context, String dbPath) {

        super(context, dbPath, null, 1);
        this.dbPath = dbPath;
        this.context = context;
    }

    public static void initInstance(Context context, String dbPath) {
        if (instance != null) {
            instance.close();
        }
        instance = new DataBaseHelper(context, dbPath);
    }

    public static DataBaseHelper getInstance() {
        return instance;
    }

    public boolean checkDataBase() {

        boolean res = false;

        SQLiteDatabase checkDB = null;

        try {
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
            checkDB.getVersion();
            res = true;
        } catch (SQLiteException e) {
            Logger.d(Constants.LOG_TAG, "Database error!!!");
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            if (checkDB != null) {
                checkDB.close();
            }
        }

        return res;
    }

    public void openDataBase() throws SQLException {
        if (database == null || !database.isOpen()) {
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
            Logger.d(Constants.LOG_TAG, "Database opened");
        }
    }

    public List<EntityItem> getChildEntities(int parentId) {
        List<EntityItem> items = new ArrayList<EntityItem>();

        Cursor cursor = null;

        try {
            cursor = database.query(ENTITY, new String[]{ID, ENTITY_DESCRIPTION, ENTITY_TYPE, ENTITY_ORDER_POSITION},
                    ENTITY_PARENT_ID + "=?", new String[]{String.valueOf(parentId)},
                    null, null, ENTITY_ORDER_POSITION);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ID));
                String description = cursor.getString(cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION));
                ItemType type = gerItemType(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_TYPE)));
                int position = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_ORDER_POSITION));

                EntityItem item = new EntityItem(id, description, type, position);
                items.add(item);
            }
        } catch (Exception ex) {
            Logger.e("getChildEntities fault", ex.getMessage(), ex);
            showText(context, "getChildEntities fault" + ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    private ItemType gerItemType(int type){
        return  type == 0 ? ItemType.Directory : ItemType.File;
    }

    public List<FindNoteItem> findNotes(String text) {

        List<FindNoteItem> items = new ArrayList<FindNoteItem>();

        String sql = "SELECT Entity.id, Entity.description " +
                "FROM Entity, EntityData " +
                "WHERE Entity.id=EntityData.id AND EntityData.TextData LIKE ? ESCAPE '#'";

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(sql, new String[]{"%" + text + "%"});
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ID));
                String description = cursor.getString(cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION));

                FindNoteItem item = new FindNoteItem(id, description, ItemType.File,
                        Utils.getSeparatedText("/", getDescriptions(id)));
                items.add(item);
            }
        } catch (Exception ex) {
            Logger.e("findNotes fault", ex.getMessage(), ex);
            showText(context, "findNotes fault" + ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Collections.sort(items);
        return items;
    }

    public List<String> getDescriptions(int currentId) {

        List<String> items = new ArrayList<String>();

        while (true) {

            Cursor cursor = null;

            try {
                cursor = database.query(ENTITY, new String[]{ENTITY_DESCRIPTION, ENTITY_PARENT_ID},
                    ID + "=?", new String[]{String.valueOf(currentId)},
                    null, null, ENTITY_ORDER_POSITION);

                if (cursor.moveToNext()) {

                    String description = cursor.getString(cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION));
                    currentId = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID));

                    items.add(description);
                }else {
                    break;
                }
            } catch (Exception ex) {
                Logger.e("getDescriptions fault", ex.getMessage(), ex);
                showText(context, "getDescriptions fault" + ex.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Collections.reverse(items);
        return items;
    }

    public String getEntityDataText(int id) {

        Cursor cursor = null;
        String text = "";

        try {
            cursor = database.query(ENTITY_DATA, new String[]{ENTITY_DATA_TEXT},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                int textIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DATA_TEXT);
                text = cursor.getString(textIndex);
            }
        } catch (Exception ex) {
            Logger.e("getEntityDataText fault", ex.getMessage(), ex);
            text = ex.getMessage();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return text;
    }

    public String getEntityDataHtml(int id) {

        Cursor cursor = null;
        String text = "";

        try {
            cursor = database.query(ENTITY_DATA, new String[]{ENTITY_DATA_HTML},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                int textIndex = cursor.getColumnIndex(DataBaseHelper.ENTITY_DATA_HTML);
                text = cursor.getString(textIndex);
            }
        } catch (Exception ex) {
            Logger.e("getEntityDataHtml fault", ex.getMessage(), ex);
            showText(context, "getEntityDataHtml fault");
            text = getEntityDataText(id);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return text;
    }

    public EntityItem getNextEntity(int id) {

        Cursor cursor = null;

        try {
            cursor = database.query(ENTITY, new String[]{ENTITY_PARENT_ID, ENTITY_ORDER_POSITION},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                int parentId = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID));
                int position = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_ORDER_POSITION));

                List<EntityItem> entities = getChildEntities(parentId);

                return getNextEntity(entities, position);
            }
        } catch (Exception ex) {
            Logger.e("getNextEntity fault", ex.getMessage(), ex);
            showText(context, "getNextEntity fault");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private EntityItem getNextEntity(List<EntityItem> entities, int position) {
        class SortAscending implements Comparator<EntityItem>
        {
            public int compare(EntityItem a, EntityItem b)
            {
                return a.getPosition() - b.getPosition();
            }
        }

        Collections.sort(entities, new SortAscending());
        for(EntityItem item : entities) {
            if(item.getPosition() > position) {
                if (item.getType() == ItemType.File) {
                    return item;
                }
                List<EntityItem> folderEntities = getChildEntities(item.getId());
                return getNextEntity(folderEntities, -1);
            }
        }

        //up
        int parentId = getParentId(entities.get(0).getId());
        if(parentId == EntityHelper.ROOT) {
            return null;
        }
        EntityItem parentEntity = getEntity(parentId);
        List<EntityItem> folderEntities = getChildEntities(parentEntity.getParentId());
        return getNextEntity(folderEntities, parentEntity.getPosition());
    }

    public EntityItem getPreviousEntity(int id) {

        Cursor cursor = null;

        try {
            cursor = database.query(ENTITY, new String[]{ENTITY_PARENT_ID, ENTITY_ORDER_POSITION},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToNext()) {
                int parentId = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID));
                int position = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_ORDER_POSITION));

                List<EntityItem> entities = getChildEntities(parentId);
                return getPreviousEntity(entities, position);
            }
        } catch (Exception ex) {
            Logger.e("GetNextEntity fault", ex.getMessage(), ex);
            showText(context, R.string.html_read_fault);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private EntityItem getPreviousEntity(List<EntityItem> entities, int position) {
        class SortDescending implements Comparator<EntityItem>
        {
            public int compare(EntityItem a, EntityItem b)
            {
                return b.getPosition() - a.getPosition();
            }
        }

        Collections.sort(entities, new SortDescending());
        for(EntityItem item : entities) {
            if(item.getPosition() < position) {
                if (item.getType() == ItemType.File) {
                    return item;
                }
                List<EntityItem> folderEntities = getChildEntities(item.getId());
                Collections.sort(folderEntities, new SortDescending());
                return getPreviousEntity(folderEntities, folderEntities.get(0).getPosition() + 1);
            }
        }

        //up
        int parentId = getParentId(entities.get(0).getId());
        if(parentId == EntityHelper.ROOT) {
            return null;
        }
        EntityItem parentEntity = getEntity(parentId);
        List<EntityItem> folderEntities = getChildEntities(parentEntity.getParentId());
        return getPreviousEntity(folderEntities, parentEntity.getPosition());
    }

    public int getParentId(int id) {

        Cursor cursor = null;
        int parentId = EntityHelper.ROOT;

        try {
            cursor = database.query(ENTITY, new String[]{ENTITY_PARENT_ID},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

            if (cursor.moveToNext()) {
                parentId = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID));
            }
        } catch (Exception ex) {
            Logger.e("GetParentId fault", ex.getMessage(), ex);
            showText(context, R.string.html_read_fault);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return parentId;
    }

    private EntityItem getEntity(int id) {

        Cursor cursor = null;

        try {
            cursor = database.query(ENTITY, new String[]{ENTITY_PARENT_ID, ENTITY_DESCRIPTION, ENTITY_TYPE, ENTITY_ORDER_POSITION},
                    ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

            if (cursor.moveToNext()) {
                int parentId = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_PARENT_ID));
                String description = cursor.getString(cursor.getColumnIndex(DataBaseHelper.ENTITY_DESCRIPTION));
                ItemType type = gerItemType(cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_TYPE)));
                int position = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.ENTITY_ORDER_POSITION));
                return new EntityItem(id, description, type, position, parentId);
            }
        } catch (Exception ex) {
            Logger.e("getEntity fault", ex.getMessage(), ex);
            showText(context, "getEntity fault" + ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public synchronized void close() {

        if (database != null)
            database.close();

        super.close();
        Logger.d(Constants.LOG_TAG, "Database closed");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
