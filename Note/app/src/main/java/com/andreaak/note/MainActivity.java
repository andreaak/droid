package com.andreaak.note;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.EmailHolder;
import com.andreaak.note.utils.GoogleDriveHelper;
import com.andreaak.note.utils.SharedPreferencesHelper;
import com.andreaak.note.utils.UT;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;


import java.io.File;

public class MainActivity extends Activity implements GoogleDriveHelper.ConnectCBs {

    private static final int REQUEST_FILE_CHOOSER = 1;
    private static final int REQ_ACCPICK = 2;
    private  EmailHolder emailHolder;
    private  Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferencesHelper.initInstance(this);
        emailHolder = new EmailHolder(SharedPreferencesHelper.getInstance());

        GoogleDriveHelper.initInstance(emailHolder);
        if (savedInstanceState == null) {
            UT.init(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.setGroupVisible(R.id.group1, false);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect: {
                getfile();
                return true;
            }
            case R.id.menu_select_account: {
                startActivityForResult(AccountPicker.newChooseAccountIntent(
                        null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQ_ACCPICK);
                return true;
            }
            case R.id.menu_download: {
                startActivityForResult(AccountPicker.newChooseAccountIntent(
                        null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQ_ACCPICK);
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void getfile() {
        Intent intent1 = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent1, REQUEST_FILE_CHOOSER);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FILE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(FileChooserActivity.PATH);
                    checkDatabase(path);
                }
                break;
            case REQ_ACCPICK:
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null)
                    emailHolder.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                if (!GoogleDriveHelper.getInstance().init(this)) {
                    Toast.makeText(this, R.string.no_google_account, Toast.LENGTH_LONG).show();
                    Log.d(Constants.LOG_TAG, getString(R.string.no_google_account));
                } else {
                    GoogleDriveHelper.getInstance().connect();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkDatabase(String path) {

        DataBaseHelper.initInstance(this, path);

        DataBaseHelper databaseHelper = DataBaseHelper.getInstance();

        boolean dbExist = databaseHelper.checkDataBase();
        if (dbExist) {
            String savePath = new File(path).getParent();
            SharedPreferencesHelper.getInstance().save(FileChooserActivity.SAVED_PATH, savePath);

            Intent intent = new Intent(this, EntityChooserActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.db_fault, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnOK() {
        menu.setGroupVisible(R.id.group1, true);
    }

    @Override
    public void onConnFail(Exception ex) {
        Toast.makeText(this, R.string.google_error, Toast.LENGTH_LONG).show();
        Log.d(Constants.LOG_TAG, ex.getMessage(), ex);
    }
}
