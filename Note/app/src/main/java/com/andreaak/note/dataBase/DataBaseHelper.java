package com.andreaak.note.dataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String STANDART_DB_PATH = "/data/data/com.andreaak.note/databases/";

    public static String ENTITY = "Entity";
    public static String ID = "ID";
    public static String PARENT_ID = "ParentID";
    public static String ORDER_POSITION = "OrderPosition";
    public static String TYPE = "Type";
    public static String DESCRIPTION = "Description";

    private static String ENTITY_DATA = "EntityData";
    public static String TEXT = "TextData";
    public static String DATA = "Data";

    private SQLiteDatabase database;

    private final Context myContext;

    private String dbPath;
    private String dbName;

    public DataBaseHelper(Context context, String dbPath, String dbName) {

        super(context, dbName, null, 1);
        this.myContext = context;
        this.dbPath = dbPath;// + "/";
        this.dbName = dbName;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     */
    public boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = dbPath;// + dbName;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {

            //database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        String inFileName = dbPath;// + dbName;

        //Open your local db as the input stream
        InputStream myInput = new FileInputStream(inFileName);

        // Path to the just created empty db
        String outFileName = STANDART_DB_PATH + dbName;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = dbPath;// + dbName;
        database = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

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

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    public Cursor GetEntities(int parentId) {
        return database.query(ENTITY, null, PARENT_ID + "=?", new String[]{String.valueOf(parentId)}, null, null, ORDER_POSITION);
    }

    public String GetEntityData(int id) {
        Cursor cursor = database.query(ENTITY_DATA, null, ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        String text = "";
        if(cursor.moveToNext()) {
            int textIndex = cursor.getColumnIndex(DataBaseHelper.DATA);
            text = cursor.getString(textIndex);
        }

        cursor.close();
        return text;
    }
}
