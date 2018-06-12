package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.activities.helpers.SelectLessonAndLanguageHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.utils.Utils;

public class CardChooseActivity extends HandleExceptionActivity implements View.OnClickListener {

    private static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 2;

    ImageButton buttonOpenLastLesson;
    TextView textViewLastLesson;
    ImageButton buttonOpenLesson;

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
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        selectLessonAndLanguage(AppConfigs.getInstance().WorkingDir);
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
}
