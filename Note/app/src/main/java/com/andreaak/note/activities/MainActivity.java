package com.andreaak.note.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.common.activitiesShared.FileChooserActivity;
import com.andreaak.common.activitiesShared.GoogleFilesChooserActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.google.EmailHolder;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.GoogleItems;
import com.andreaak.common.google.IGoogleActivity;
import com.andreaak.common.google.OperationGoogleDrive;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.FileLogger;
import com.andreaak.common.utils.logger.ILogger;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.common.utils.logger.NativeLogger;
import com.andreaak.note.R;
import com.andreaak.note.configs.AppConfigs;
import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.predicates.DatabaseNamePredicate;
import com.andreaak.note.predicates.DatabasePredicate;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import static com.andreaak.common.utils.Utils.showText;

public class MainActivity extends Activity implements IGoogleActivity {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;
    private static final int REQUEST_PREFERENCES = 4;

    private EmailHolder emailHolder;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private boolean isPrefChanged;

    private Menu menu;
    private GoogleDriveHelper googleDriveHelper;
    private OperationGoogleDrive operationGoogleDriveHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        googleDriveHelper = (GoogleDriveHelper) getLastNonConfigurationInstance();
        if (googleDriveHelper == null) {
            SharedPreferencesHelper.initInstance(this);
            GoogleDriveHelper.initInstance(new EmailHolder());
            googleDriveHelper = GoogleDriveHelper.getInstance();
            emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
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
        operationGoogleDriveHelper = new OperationGoogleDrive(
                this,
                getString(R.string.app_name),
                com.andreaak.note.R.id.groupGoogle);
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return googleDriveHelper;
    }

    @Override
    protected void onRestart() {
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.setGroupVisible(R.id.groupGoogle, googleDriveHelper.isConnected());
        this.menu = menu;
        operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect: {
                getFile();
                return true;
            }
            case R.id.menu_select_account: {
                try {
                    startActivityForResult(AccountPicker.newChooseAccountIntent(
                            null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQUEST_GOOGLE_CONNECT);
                } catch (Exception e) {
                    Logger.d(Constants.LOG_TAG, "Google services problem");
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                }
                return true;
            }
            case R.id.menu_download: {
                getGoogleFiles();
                return true;
            }
            case R.id.menu_exit: {
                finish();
                return true;
            }
            case R.id.menu_settings: {
                isPrefChanged = false;
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_PREFERENCES);
                return true;
            }
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
            case REQUEST_GOOGLE_CONNECT:
                operationGoogleDriveHelper.connectGoogleDrive(data, this, googleDriveHelper);
                break;
            case REQUEST_GOOGLE_FILES_CHOOSER:
                if (resultCode == RESULT_OK) {
                    GoogleItems items = (GoogleItems) data.getSerializableExtra(GoogleFilesChooserActivity.ITEMS);
                    String path = data.getStringExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH);
                    downloadFromGoogleDrive(items, AppConfigs.getInstance().DownloadDir);
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

    private void getGoogleFiles() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        intent.putExtra(GoogleFilesChooserActivity.PREDICATE, new DatabaseNamePredicate());
        intent.putExtra(GoogleFilesChooserActivity.TITLE, getString(com.andreaak.note.R.string.app_name));
        intent.putExtra(GoogleFilesChooserActivity.GOOGLE_DRIVE_PATH, AppConfigs.getInstance().GoogleDir);
        intent.putExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH_INITIAL, AppConfigs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final GoogleItems items, final String path) {
        if (items.getItems().length == 0) {
            return;
        }

        menu.setGroupVisible(R.id.groupGoogle, false);

        googleDriveHelper.saveFiles(items, path);
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

    @Override
    public void onFinished() {

    }
}
