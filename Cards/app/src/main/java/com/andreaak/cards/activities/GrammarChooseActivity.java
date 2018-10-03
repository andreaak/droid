package com.andreaak.cards.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.FileArrayAdapter;
import com.andreaak.cards.activities.helpers.FileHelper;
import com.andreaak.cards.activities.helpers.FileItem;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.OperationGoogleDrive;
import com.andreaak.common.google.SyncHelper;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.List;

public class GrammarChooseActivity extends ListActivity {

    //in
    public static final String PATH = "path";

    private static final int REQUEST_GOOGLE_CONNECT = 2;

    private FileArrayAdapter adapter;
    private FileHelper helper;

    private Menu menu;
    private GoogleDriveHelper googleDriveHelper;
    private OperationGoogleDrive operationGoogleDriveHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (FileHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            String path = getIntent().getStringExtra(PATH);
            helper = new FileHelper(this, path);
        }

        fill(helper.getCurrentPath());

        googleDriveHelper = GoogleDriveHelper.getInstance();
        operationGoogleDriveHelper = new OperationGoogleDrive(
                this,
                getString(R.string.select_lesson),
                com.andreaak.cards.R.id.groupGoogle);
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
    }

    @Override
    protected void onRestart() {
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        super.onRestart();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verb_choose, menu);
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, googleDriveHelper.isConnected());
        this.menu = menu;
        operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                downloadFiles();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_CONNECT:
                operationGoogleDriveHelper.connectGoogleDrive(data, this, googleDriveHelper);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void downloadFiles() {
        new SyncHelper(AppConfigs.getInstance().getGrammarDir(), AppConfigs.getInstance().getRemoteGrammarDir(), operationGoogleDriveHelper)
                .process();
    }

    private void fill(String currentPath) {
        List<FileItem> dir = helper.getEntities(currentPath);
        setTitle(helper.getDescriptions(currentPath));
        adapter = new FileArrayAdapter(GrammarChooseActivity.this, R.layout.activity_file_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            fill(item.getPath());
        } else {
            onNoteClick(item);
        }
    }

    private void onNoteClick(FileItem item) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra(HtmlActivity.PATH, item.getPath());
        intent.putExtra(HtmlActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
