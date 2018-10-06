package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.activities.helpers.SelectLessonAndLanguageHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.predicates.LessonFileNamePredicate;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.GoogleFilesChooserActivity;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.GoogleItems;
import com.andreaak.common.google.IGoogleActivity;
import com.andreaak.common.google.OperationGoogleDrive;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

public class LessonChooseActivity extends HandleExceptionActivity implements IGoogleActivity, View.OnClickListener {

    private static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 1;

    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;

    ImageButton buttonOpenLastLesson;
    TextView textViewLastLesson;
    ImageButton buttonOpenLesson;

    private Menu menu;
    private GoogleDriveHelper googleDriveHelper;
    private OperationGoogleDrive operationGoogleDriveHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_choose);

        buttonOpenLastLesson = (ImageButton) findViewById(R.id.buttonOpenLastLesson);
        buttonOpenLastLesson.setOnClickListener(this);
        String lastLesson = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_PATH);
        if (Utils.isEmpty(lastLesson)) {
            textViewLastLesson = (TextView) findViewById(R.id.textViewLastLesson);
            textViewLastLesson.setEnabled(false);
            buttonOpenLastLesson.setEnabled(false);
        }

        buttonOpenLesson = (ImageButton) findViewById(R.id.buttonOpenLesson);
        buttonOpenLesson.setOnClickListener(this);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {

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
            case REQUEST_LESSON_AND_LANGUAGE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    SelectLessonAndLanguageHelper selectHelper = (SelectLessonAndLanguageHelper) data.getSerializableExtra(SelectLessonAndLanguageActivity.HELPER);
                    if (selectHelper.lessonItem.isContainsWords()) {
                        openCard(selectHelper.lessonItem);
                    }
                }
                break;
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonOpenLastLesson:
                openLastLesson();
                break;
            case R.id.buttonOpenLesson:
                openLesson();
                break;
        }
    }

    private void openLastLesson() {
        String lastLesson = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_PATH);
        String lastLanguage = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_LANGUAGE);
        LessonItem lessonItem = XmlParser.parseLesson(lastLesson);

        LanguageItem languageItem = LanguageItem.getItem(lastLanguage);
        lessonItem.setLanguageItem(languageItem);
        openCard(lessonItem);
    }

    private void openLesson() {
        selectLessonAndLanguage(AppConfigs.getInstance().getLessonsDir());
    }

    private void selectLessonAndLanguage(String path) {
        Intent intent = new Intent(this, SelectLessonAndLanguageActivity.class);
        intent.putExtra(SelectLessonAndLanguageActivity.DIRECTORY, path);
        startActivityForResult(intent, REQUEST_LESSON_AND_LANGUAGE_CHOOSER);
    }

    private void openCard(LessonItem lessonItem) {
        CardActivityHelper helper = new CardActivityHelper();
        helper.lessonItem = lessonItem;
        helper.currentWord = helper.lessonItem.getWords().get(0);

        saveLastLesson(lessonItem);

        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra(CardActivity.HELPER, helper);
        startActivity(intent);
    }

    private void saveLastLesson(LessonItem lessonItem) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_LAST_LESSON_PATH, lessonItem.getPath());
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_LAST_LESSON_LANGUAGE,
                lessonItem.getLanguageItem().toString());
    }

    private void chooseFilesForDownload() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        intent.putExtra(GoogleFilesChooserActivity.PREDICATE, new LessonFileNamePredicate());
        intent.putExtra(GoogleFilesChooserActivity.TITLE, getString(R.string.select_lesson));
        intent.putExtra(GoogleFilesChooserActivity.GOOGLE_DRIVE_PATH, AppConfigs.getInstance().getRemoteLessonsDir());
        intent.putExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH_INITIAL, AppConfigs.getInstance().getLessonsDir());
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

    }
}
