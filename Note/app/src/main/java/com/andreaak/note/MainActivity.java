package com.andreaak.note;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.andreaak.note.dataBase.DataBaseHelper;

import java.io.File;

public class MainActivity extends Activity {

    private static final int REQUEST_FILE_CHOOSER = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_connect) {
            getfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getfile() {
        Intent intent1 = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent1, REQUEST_FILE_CHOOSER);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_CHOOSER) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(FileChooserActivity.PATH);
                checkDatabase(path);
            }
        }
    }

    private void checkDatabase(String path) {

        DataBaseHelper.initInstance(this, path);

        DataBaseHelper databaseHelper = DataBaseHelper.getInstance();

        boolean dbExist = databaseHelper.checkDataBase();
        if (dbExist) {
            SharedPreferences sPref = getSharedPreferences(FileChooserActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            String savePath = new File(path).getParent();
            ed.putString(FileChooserActivity.SAVED_PATH, savePath);
            ed.commit();

            Intent intent = new Intent(this, EntityChooserActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.db_fault), Toast.LENGTH_LONG).show();
        }
    }
}
