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
import com.andreaak.cards.activities.helpers.VerbActivityHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.predicates.IrregularVerbEnFileNamePredicate;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.GoogleFilesChooserActivity;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.GoogleItems;
import com.andreaak.common.google.IGoogleActivity;
import com.andreaak.common.google.OperationGoogleDrive;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.List;

public class VerbChooseActivity extends ListActivity implements IGoogleActivity {

    //in
    public static final String PATH = "path";

    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;

    private FileArrayAdapter adapter;
    private FileHelper fileHelper;

    private Menu menu;
    private GoogleDriveHelper googleDriveHelper;
    private OperationGoogleDrive operationGoogleDriveHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        fileHelper = (FileHelper) getLastNonConfigurationInstance();
        if (fileHelper == null) {
            String path = getIntent().getStringExtra(PATH);
            fileHelper = new FileHelper(this, path);
        }
        fill(fileHelper.getCurrentPath());

        googleDriveHelper = GoogleDriveHelper.getInstance();
        operationGoogleDriveHelper = new OperationGoogleDrive(
                this,
                getString(R.string.select_lesson),
                com.andreaak.cards.R.id.groupGoogle);
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        setTitle(com.andreaak.cards.R.string.select_lesson);
    }

    @Override
    protected void onRestart() {
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        super.onRestart();
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
                } catch (Exception ex) {
                    Logger.d(Constants.LOG_TAG, "Google services problem");
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
                return true;
            }
            case com.andreaak.cards.R.id.menu_download: {
                chooseFilesForDownload();
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
            case REQUEST_GOOGLE_FILES_CHOOSER:
                if (resultCode == RESULT_OK) {
                    GoogleItems items = (GoogleItems) data.getSerializableExtra(GoogleFilesChooserActivity.ITEMS);
                    String path = data.getStringExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH);
                    downloadFromGoogleDrive(items, path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void fill(String currentPath) {
        List<FileItem> dir = fileHelper.getEntities(currentPath);
        setTitle(fileHelper.getDescriptions(currentPath));
        adapter = new FileArrayAdapter(VerbChooseActivity.this, R.layout.activity_file_chooser, dir);
        this.setListAdapter(adapter);
    }

    private void onNoteClick(FileItem item) {
        VerbLessonItem lessonItem = XmlParser.parseVerbLesson(item.getPath());
        VerbActivityHelper helper = new VerbActivityHelper();
        helper.lessonItem = lessonItem;
        helper.currentWord = helper.lessonItem.getWords().get(0);

        Intent intent = new Intent(this, VerbActivity.class);
        intent.putExtra(CardActivity.HELPER, helper);
        startActivity(intent);
    }

    private void chooseFilesForDownload() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        intent.putExtra(GoogleFilesChooserActivity.PREDICATE, new IrregularVerbEnFileNamePredicate());
        intent.putExtra(GoogleFilesChooserActivity.TITLE, getString(R.string.select_lesson));
        intent.putExtra(GoogleFilesChooserActivity.GOOGLE_DRIVE_PATH, AppConfigs.getInstance().getRemoteIrregularVerbDir());
        intent.putExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH_INITIAL, AppConfigs.getInstance().getIrregularVerbDir());
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final GoogleItems items, final String path) {
        if (items.getItems().length == 0) {
            return;
        }

        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false);

        googleDriveHelper.saveFiles(items, path);
    }

    @Override
    public void onFinished() {
        fill(fileHelper.getCurrentPath());
    }
}
