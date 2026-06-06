package com.andreaak.note.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.common.activitiesShared.FileChooserActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;

import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.FileLogger;
import com.andreaak.common.utils.logger.ILogger;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.common.utils.logger.NativeLogger;
import com.andreaak.note.R;
import com.andreaak.note.configs.AppConfigs;
import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.predicates.DatabasePredicate;

import static com.andreaak.common.utils.Utils.showText;

public class MainActivity extends Activity {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQUEST_PREFERENCES = 4;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private boolean isPrefChanged;

    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        SharedPreferencesHelper.initInstance(this);
        Utils.init(this);
        AppConfigs.getInstance().init(this);
        AppConfigs.getInstance().read();
        setLogger();
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                isPrefChanged = true;
            }
        };

        SharedPreferencesHelper.getInstance().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(prefListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_connect)  {
            getFile();
            return true;
        } else if(id == R.id.menu_exit)  {
            finish();
            return true;
        } else if(id == R.id.menu_settings)  {
            isPrefChanged = false;
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_PREFERENCES);
            return true;
        } else if(id == R.id.menu_download)  {
            googleDriveFiles();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FILE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(FileChooserActivity.FILE_PATH);
                    checkDatabase(path);
                }
                break;
            case REQUEST_PREFERENCES:
                if (isPrefChanged) {
                    AppConfigs.getInstance().read();
                    setLogger();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLogger() {
        ILogger log = AppConfigs.getInstance().IsLoggingActive ? new FileLogger() : new NativeLogger();
        Logger.setLogger(log);
    }

    private void getFile() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.PREDICATE, new DatabasePredicate());
        intent.putExtra(FileChooserActivity.TITLE, getString(R.string.select_file));
        intent.putExtra(FileChooserActivity.INITIAL_PATH, AppConfigs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_FILE_CHOOSER);
    }

    private void checkDatabase(String path) {

        DataBaseHelper.initInstance(this, path);

        DataBaseHelper databaseHelper = DataBaseHelper.getInstance();

        boolean dbExist = databaseHelper.checkDataBase();
        if (dbExist) {
            Intent intent = new Intent(this, EntityChooserActivity.class);
            startActivity(intent);
        } else {
            showText(this, R.string.db_fault);
        }
    }

    private void googleDriveFiles() {
        Intent intent = new Intent(this, DriveActivity.class);
        intent.putExtra(DriveActivity.PATH, AppConfigs.getInstance().DownloadDir);
        intent.putExtra(DriveActivity.PREFIXES, "");
        startActivity(intent);
    }
}
