package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.R;
import com.andreaak.cards.activitiesShared.FileChooserActivity;
import com.andreaak.cards.helpers.SelectLanguageHelper;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.helpers.CardActivityHelper;
import com.andreaak.cards.helpers.SelectLessonAndLanguageHelper;
import com.andreaak.cards.activitiesShared.DirectoryChooserActivity;
import com.andreaak.cards.configs.Configs;
import com.andreaak.cards.predicates.LessonXmlDirectoryPredicate;
import com.andreaak.cards.activitiesShared.HandleExceptionActivity;
import com.andreaak.cards.configs.SharedPreferencesHelper;
import com.andreaak.cards.predicates.LessonXmlPredicate;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.XmlParser;

import java.io.File;

public class CardChooseActivity extends HandleExceptionActivity implements View.OnClickListener {

    private static final int REQUEST_LESSONS_DIRECTORY_CHOOSER = 1;
    private static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 2;
    private static final int REQUEST_LESSON_FILE_CHOOSER = 3;
    private static final int REQUEST_LANGUAGE_CHOOSER = 5;

    ImageButton buttonOpenLastLesson;
    ImageButton buttonOpenLesson;
    ImageButton buttonOpenLessonsFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Thread.setDefaultUncaughtExceptionHandler(new ActivityExceptionHandler(this));

        setContentView(R.layout.activity_card_choose);

        buttonOpenLastLesson = (ImageButton) findViewById(R.id.buttonOpenLastLesson);
        buttonOpenLastLesson.setOnClickListener(this);
        String lastLesson = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_PATH);
        if (Utils.isEmpty(lastLesson)) {
            buttonOpenLastLesson.setEnabled(false);
        }

        buttonOpenLesson = (ImageButton) findViewById(R.id.buttonOpenLesson);
        buttonOpenLesson.setOnClickListener(this);

        buttonOpenLessonsFolder = (ImageButton) findViewById(R.id.buttonOpenLessonsFolder);
        buttonOpenLessonsFolder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonOpenLastLesson:
                openLastLesson();
                break;
            case R.id.buttonOpenLesson:
                getLessonFile();
                break;
            case R.id.buttonOpenLessonsFolder:
                getLessonsDirectory();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LESSONS_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    selectLessonAndLanguage(path);
                    SharedPreferencesHelper.getInstance().save(Configs.SP_DIRECTORY_WITH_LESSONS_PATH, path);
                }
                break;
            case REQUEST_LESSON_AND_LANGUAGE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    SelectLessonAndLanguageHelper selectHelper = (SelectLessonAndLanguageHelper) data.getSerializableExtra(SelectLessonAndLanguageActivity.HELPER);
                    if (selectHelper.lessonItem.isContainsWords()) {
                        openCard(selectHelper.lessonItem);
                    }
                }
                break;
            case REQUEST_LESSON_FILE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra(FileChooserActivity.FILE_PATH);
                    String path = new File(filePath).getParent();
                    SharedPreferencesHelper.getInstance().save(Configs.SP_DIRECTORY_WITH_LESSONS_PATH, path);
                    selectLanguage(filePath);
                }
                break;
            case REQUEST_LANGUAGE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    SelectLanguageHelper selectHelper = (SelectLanguageHelper) data.getSerializableExtra(SelectLanguageActivity.HELPER);
                    if (selectHelper.lessonItem.isContainsWords()) {
                        openCard(selectHelper.lessonItem);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openLastLesson() {
        String lastLesson = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_PATH);
        String lastLanguage = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_LANGUAGE);
        LessonItem lessonItem = XmlParser.parseLesson(lastLesson);

        LanguageItem languageItem = LanguageItem.getItem(lastLanguage);
        lessonItem.setLanguageItem(languageItem);
        openCard(lessonItem);
    }


    private void getLessonFile() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.PREDICATE, new LessonXmlPredicate());
        intent.putExtra(FileChooserActivity.TITLE, getString(R.string.select_lesson));
        String initialPath = SharedPreferencesHelper.getInstance().getString(Configs.SP_DIRECTORY_WITH_LESSONS_PATH);
        intent.putExtra(FileChooserActivity.INITIAL_PATH, initialPath);
        startActivityForResult(intent, REQUEST_LESSON_FILE_CHOOSER);
    }

    private void selectLanguage(String path) {
        Intent intent = new Intent(this, SelectLanguageActivity.class);
        intent.putExtra(SelectLanguageActivity.FILE, path);
        startActivityForResult(intent, REQUEST_LANGUAGE_CHOOSER);
    }

    private void getLessonsDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new LessonXmlDirectoryPredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_lessons_folder));
        String initialPath = SharedPreferencesHelper.getInstance().getString(Configs.SP_DIRECTORY_WITH_LESSONS_PATH);
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, initialPath);
        startActivityForResult(intent, REQUEST_LESSONS_DIRECTORY_CHOOSER);
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
        SharedPreferencesHelper.getInstance().save(Configs.SP_LAST_LESSON_PATH, lessonItem.getPath());
        SharedPreferencesHelper.getInstance().save(Configs.SP_LAST_LESSON_LANGUAGE,
                lessonItem.getLanguageItem().toString());
    }
}