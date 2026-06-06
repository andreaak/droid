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
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.utils.XmlParser;

import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;

import com.andreaak.common.utils.Utils;


public class LessonChooseActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 1;

    ImageButton buttonOpenLastLesson;
    TextView textViewLastLesson;
    ImageButton buttonOpenLesson;

    private Menu menu;
//    private GoogleDriveHelper googleDriveHelper;
//    private OperationGoogleDrive operationGoogleDriveHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lesson_choose);

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
    }

    @Override
    protected void onRestart() {
        //googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verb_choose, menu);
        //menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, googleDriveHelper.isConnected());
        this.menu = menu;
        //operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case REQUEST_LESSON_AND_LANGUAGE_CHOOSER:
//                if (resultCode == RESULT_OK) {
//                    LessonItem lessonItem = (LessonItem) data.getSerializableExtra(CardActivity.HELPER);
//                    if (lessonItem.isContainsWords()) {
//                        openCard(lessonItem);
//                    }
//                }
//                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonOpenLastLesson)  {
            openLastLesson();
        } else if(id == R.id.buttonOpenLesson)  {
            openLesson();
        }
    }

    private void openLastLesson() {
        String lastLesson = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_PATH);
        String lastLanguage = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_LANGUAGE);
        String lastPrefix = SharedPreferencesHelper.getInstance().getString(AppConfigs.SP_LAST_LESSON_PREFIX);
        LessonItem lessonItem = XmlParser.parseLesson(lastLesson, lastPrefix);

        LanguageItem languageItem = LanguageItem.getLanguageItem(lastLanguage);
        lessonItem.setLanguageItem(languageItem);
        openCard(lessonItem);
    }

    private void openLesson() {
        Intent intent = new Intent(this, SelectLessonAndLanguageActivity.class);
        intent.putExtra(SelectLessonAndLanguageActivity.DIRECTORY, AppConfigs.getInstance().getLessonsDir());
        intent.putExtra(SelectLessonAndLanguageActivity.PREFIX, AppConfigs.getInstance().LessonsPrefix);
        intent.putExtra(SelectLessonAndLanguageActivity.SORT_ITEMS, "true");
        startActivityForResult(intent, REQUEST_LESSON_AND_LANGUAGE_CHOOSER);
    }

    private void openCard(LessonItem lessonItem) {
        CardActivityHelper helper = new CardActivityHelper();
        helper.lessonItem = lessonItem;
        helper.currentWord = helper.lessonItem.getLessonWords().get(0);

        saveLastLesson(lessonItem);

        Intent intent;
        if(lessonItem.getFileName().contains("_html")) {
            intent = new Intent(this, CardHtmlActivity.class);
            intent.putExtra(CardHtmlActivity.HELPER, helper);

        } else {
            intent = new Intent(this, CardActivity.class);
            intent.putExtra(CardActivity.HELPER, helper);
        }
        startActivity(intent);
    }

    private void saveLastLesson(LessonItem lessonItem) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_LAST_LESSON_PATH, lessonItem.getPath());
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_LAST_LESSON_LANGUAGE,
                lessonItem.getLanguageItem().toString());
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_LAST_LESSON_PREFIX,
                lessonItem.getPrefix());
    }
}
