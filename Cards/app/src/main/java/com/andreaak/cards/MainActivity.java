package com.andreaak.cards;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.andreaak.cards.google.EmailHolder;
import com.andreaak.cards.google.GoogleDriveHelper;
import com.andreaak.cards.google.IConnectGoogleDrive;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.SharedPreferencesHelper;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.logger.FileLogger;
import com.andreaak.cards.utils.logger.ILogger;
import com.andreaak.cards.utils.logger.Logger;
import com.andreaak.cards.utils.logger.NativeLogger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import static com.andreaak.cards.utils.Utils.showText;

public class MainActivity extends Activity implements IConnectGoogleDrive {

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
        setContentView(com.andreaak.cards.R.layout.activity_main);
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
        getMenuInflater().inflate(com.andreaak.cards.R.menu.menu_main, menu);
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, helper.isConnected());
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.andreaak.cards.R.id.menu_open_cards: {
                openCards();
                return true;
            }
            case com.andreaak.cards.R.id.menu_select_account: {
                try {
                    startActivityForResult(AccountPicker.newChooseAccountIntent(
                            null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQUEST_GOOGLE_CONNECT);
                } catch (Exception e) {
                    Logger.d(Constants.LOG_TAG, "Google services problem");
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                }
                return true;
            }
            case com.andreaak.cards.R.id.menu_download: {
                getGoogleFiles();
                return true;
            }
            case com.andreaak.cards.R.id.menu_exit: {
                finish();
                return true;
            }
            case com.andreaak.cards.R.id.menu_settings: {
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
            case REQUEST_GOOGLE_CONNECT:
                setTitle(com.andreaak.cards.R.string.connecting);
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
                    emailHolder.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    if (!helper.init()) {
                        showText(this, com.andreaak.cards.R.string.no_google_account);
                        setTitle(com.andreaak.cards.R.string.app_name);
                        Logger.d(Constants.LOG_TAG, getString(com.andreaak.cards.R.string.no_google_account));
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

    private void openCards() {
        Intent intent = new Intent(this, CardActivity.class);
        startActivity(intent);
    }

    private void getGoogleFiles() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final String[] ids, final String[] names, final String path) {
        if (ids.length == 0) {
            return;
        }

        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false);

        helper.saveFiles(ids, names, path);
    }

    @Override
    public void onConnectionOK() {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, true);
        setTitle(com.andreaak.cards.R.string.app_name);
    }

    @Override
    public void onConnectionFail(Exception ex) {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false);
        showText(this, com.andreaak.cards.R.string.google_error);
        setTitle(com.andreaak.cards.R.string.app_name);
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    @Override
    public void onDownloadProgress(String message) {
        setTitle(message);
    }

    @Override
    public void onDownloadFinished(Exception ex) {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, true);
        if (ex == null) {
            showText(this, com.andreaak.cards.R.string.download_success);
        } else {
            showText(this, com.andreaak.cards.R.string.download_fault);
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        setTitle(com.andreaak.cards.R.string.app_name);
    }
}