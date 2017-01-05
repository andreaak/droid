package com.andreaak.note;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.dataBase.DataBaseHelper;

public class MainActivity extends Activity {

    private static final int REQUEST_PATH = 1;
    DataBaseHelper myDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //edittext = (EditText) findViewById(R.id.editText);
    }

    public void getfile(View view) {
        Intent intent1 = new Intent(this, FileChooser.class);
        startActivityForResult(intent1, REQUEST_PATH);
    }

    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(FileChooser.FILE_NAME);
                String path = data.getStringExtra(FileChooser.PATH);
                OpenDatabase(path, name);
            }
        }
    }

    private void OpenDatabase(String path, String name) {

        if(myDbHelper != null) {
            myDbHelper.close();
        }
        myDbHelper = new DataBaseHelper(this, path, name);


        boolean dbExist = myDbHelper.checkDataBase();

        if (dbExist) {
            try {

                myDbHelper.openDataBase();
                setTitle(Constants.getText(getString(R.string.connected), name));
            } catch (SQLException ex) {

                Log.e(Constants.LOG_TAG, ex.getMessage(), ex);
            }
        }
    }
}
