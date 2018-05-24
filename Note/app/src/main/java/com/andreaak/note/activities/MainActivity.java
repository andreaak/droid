package com.andreaak.note.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.note.R;
import com.andreaak.note.activitiesShared.FileChooserActivity;
import com.andreaak.note.activitiesShared.GoogleFilesChooserActivity;
import com.andreaak.note.configs.Configs;
import com.andreaak.note.configs.SharedPreferencesHelper;
import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.google.EmailHolder;
import com.andreaak.note.google.GoogleDriveHelper;
import com.andreaak.note.google.IOperationGoogleDrive;
import com.andreaak.note.predicates.DatabaseNamePredicate;
import com.andreaak.note.predicates.DatabasePredicate;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.Utils;
import com.andreaak.note.utils.logger.FileLogger;
import com.andreaak.note.utils.logger.ILogger;
import com.andreaak.note.utils.logger.Logger;
import com.andreaak.note.utils.logger.NativeLogger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;

import static com.andreaak.note.utils.Utils.showText;

public class MainActivity extends Activity implements IOperationGoogleDrive {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;
    private static final int REQUEST_PREFERENCES = 4;

    private EmailHolder emailHolder;
    private Menu menu;
    private GoogleDriveHelper helper;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private boolean isPrefChanged;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (GoogleDriveHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            SharedPreferencesHelper.initInstance(this);
            GoogleDriveHelper.initInstance(new EmailHolder());
            helper = GoogleDriveHelper.getInstance();
            emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
            Utils.init(this);
            Configs.init(this);
            Configs.read();
            setLogger();
            prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    isPrefChanged = true;
                }
            };

            SharedPreferencesHelper.getInstance().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(prefListener);

        }
        helper.setActivity(this);
        emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.setGroupVisible(R.id.groupGoogle, helper.isConnected());
        this.menu = menu;
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
                setTitle(R.string.connecting);
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
                    emailHolder.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    if (!helper.init()) {
                        showText(this, R.string.no_google_account);
                        setTitle(R.string.app_name);
                        Logger.d(Constants.LOG_TAG, getString(R.string.no_google_account));
                    } else {
                        helper.connect();
                    }
                } else {
                    setTitle(com.andreaak.note.R.string.app_name);
                }
                break;
            case REQUEST_GOOGLE_FILES_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String[] ids = data.getStringArrayExtra(GoogleFilesChooserActivity.IDS);
                    String[] names = data.getStringArrayExtra(GoogleFilesChooserActivity.NAMES);
                    String path = data.getStringExtra(GoogleFilesChooserActivity.PATH);
                    downloadFromGoogleDrive(ids, names, path);
                }
                break;
            case REQUEST_PREFERENCES:
                if (isPrefChanged) {
                    Configs.read();
                    setLogger();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLogger() {
        ILogger log = Configs.IsLoggingActive ? new FileLogger() : new NativeLogger();
        Logger.setLogger(log);
    }

    private void getFile() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.PREDICATE, new DatabasePredicate());
        intent.putExtra(FileChooserActivity.TITLE, getString(R.string.select_file));
        intent.putExtra(FileChooserActivity.INITIAL_PATH, Configs.FilesDir);
        startActivityForResult(intent, REQUEST_FILE_CHOOSER);
    }

    private void getGoogleFiles() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        intent.putExtra(GoogleFilesChooserActivity.PREDICATE, new DatabaseNamePredicate());
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final String[] ids, final String[] names, final String path) {
        if (ids.length == 0) {
            return;
        }

        menu.setGroupVisible(R.id.groupGoogle, false);

        helper.saveFiles(ids, names, path);
    }

    private void checkDatabase(String path) {

        DataBaseHelper.initInstance(this, path);

        DataBaseHelper databaseHelper = DataBaseHelper.getInstance();

        boolean dbExist = databaseHelper.checkDataBase();
        if (dbExist) {
            String savePath = new File(path).getParent();
            Configs.saveFilesDirectory(savePath);

            Intent intent = new Intent(this, EntityChooserActivity.class);
            startActivity(intent);
        } else {
            showText(this, R.string.db_fault);
        }
    }

    @Override
    public void onConnectionOK() {
        menu.setGroupVisible(R.id.groupGoogle, true);
        setTitle(R.string.app_name);
    }

    @Override
    public void onConnectionFail(Exception ex) {
        menu.setGroupVisible(R.id.groupGoogle, false);
        showText(this, R.string.google_error);
        setTitle(R.string.app_name);
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    @Override
    public void onOperationProgress(String message) {
        setTitle(message);
    }

    @Override
    public void onOperationFinished(Exception ex) {
        menu.setGroupVisible(R.id.groupGoogle, true);
        if (ex == null) {
            showText(this, R.string.download_success);
        } else {
            showText(this, R.string.download_fault);
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        setTitle(R.string.app_name);
    }
}
