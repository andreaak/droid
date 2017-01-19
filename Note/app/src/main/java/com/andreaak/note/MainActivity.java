package com.andreaak.note;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.google.EmailHolder;
import com.andreaak.note.google.GoogleDriveHelper;
import com.andreaak.note.google.IConnectGoogleDrive;
import com.andreaak.note.utils.Configs;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.SharedPreferencesHelper;
import com.andreaak.note.utils.logger.FileLogger;
import com.andreaak.note.utils.logger.Logger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;

import static com.andreaak.note.utils.Utils.init;
import static com.andreaak.note.utils.Utils.showText;

public class MainActivity extends Activity implements IConnectGoogleDrive {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;

    private EmailHolder emailHolder;
    private Menu menu;
    private GoogleDriveHelper helper;

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
            init(this);
            Logger.setLogger(new FileLogger());
        }
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
                startActivityForResult(AccountPicker.newChooseAccountIntent(
                        null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQUEST_GOOGLE_CONNECT);
                return true;
            }
            case R.id.menu_download: {
                getGoogleFiles();
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
                    String path = data.getStringExtra(FileChooserActivity.PATH);
                    checkDatabase(path);
                }
                break;
            case REQUEST_GOOGLE_CONNECT:
                setTitle(R.string.connecting);
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
                    emailHolder.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    if (!helper.init(this)) {
                        showText(this, R.string.no_google_account);
                        setTitle(R.string.app_name);
                        Logger.d(Constants.LOG_TAG, getString(R.string.no_google_account));
                    } else {
                        helper.connect();
                    }
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getFile() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent, REQUEST_FILE_CHOOSER);
    }

    private void getGoogleFiles() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final String[] ids, final String[] names, final String path) {
        final boolean[] isDownload = {false};
        final IConnectGoogleDrive act = this;
        if (ids.length == 0) {
            return;
        }

        setTitle(R.string.download);
        menu.setGroupVisible(R.id.groupGoogle, false);
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... nadas) {
                try {
                    boolean res = true;

                    for (int i = 0; i < ids.length; i++) {
                        File targetFile = new File(path + "/" + names[i]);
                        res = helper.saveToFile(ids[i], targetFile) && res;
                    }
                    isDownload[0] = res;
                } catch (Exception e) {
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    act.onDownloadFinished(null);
                } else {
                    Exception e = ex != null ? ex : new Exception("Undefined");
                    act.onDownloadFinished(ex);
                }
            }
        }.execute();
    }

    private void checkDatabase(String path) {

        DataBaseHelper.initInstance(this, path);

        DataBaseHelper databaseHelper = DataBaseHelper.getInstance();

        boolean dbExist = databaseHelper.checkDataBase();
        if (dbExist) {
            String savePath = new File(path).getParent();
            SharedPreferencesHelper.getInstance().save(Configs.SP_DIRECTORY_WITH_DB_PATH, savePath);

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
        showText(this, R.string.google_error);
        setTitle(R.string.app_name);
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    @Override
    public void onDownloadFinished(Exception ex) {
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
