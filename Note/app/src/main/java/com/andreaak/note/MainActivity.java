package com.andreaak.note;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.dataBase.DataBaseHelper;

public class MainActivity extends Activity {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQUEST_NOTE_CHOOSER = 2;
    private static final String HELPER = "helper";
    //DataBaseHelper myDbHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        myDbHelper = (DataBaseHelper) getLastNonConfigurationInstance();
//        if(myDbHelper != null) {
//            setTitle(Constants.getText(getString(R.string.connected), myDbHelper.getDatabaseName()));
//        }
    }

//    @Override
//    public Object onRetainNonConfigurationInstance() {
//        return myDbHelper;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_connect){
            getfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getfile() {
        Intent intent1 = new Intent(this, FileChooser.class);
        startActivityForResult(intent1, REQUEST_FILE_CHOOSER);
    }

    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_FILE_CHOOSER) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(FileChooser.FILE_NAME);
                String path = data.getStringExtra(FileChooser.PATH);
                checkDatabase(path, name);
            }
        } else if (requestCode == REQUEST_NOTE_CHOOSER) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    private void checkDatabase(String path, String fileName) {

        DataBaseHelper myDbHelper = new DataBaseHelper(this, path, fileName);

        boolean dbExist = myDbHelper.checkDataBase();
        if (dbExist) {
            Intent intent = new Intent(this, NoteChooser.class);
            intent.putExtra(FileChooser.PATH, path);
            intent.putExtra(FileChooser.FILE_NAME, fileName);
            startActivityForResult(intent, REQUEST_NOTE_CHOOSER);
        }
    }
}
