package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.SelectLessonAndLanguageHelper;
import com.andreaak.cards.adapters.LangSpinAdapter;
import com.andreaak.cards.adapters.LessonsSpinAdapter;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectLessonAndLanguageActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final String DIRECTORY = "Directory";
    public static final String HELPER = "Helper";

    private Spinner spinnerLessons;
    private Spinner spinnerLang;

    private Button buttonOk;
    private Button buttonCancel;

    private SelectLessonAndLanguageHelper helper;
    private LessonsSpinAdapter lessonsAdapter;
    private LangSpinAdapter langAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lesson_and_language);

        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        spinnerLessons = (Spinner) findViewById(R.id.spinnerLessons);
        spinnerLang = (Spinner) findViewById(R.id.spinnerLang);

        setTitle(getString(R.string.select_lesson));

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (SelectLessonAndLanguageHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
            initializeLessonsSpinner(helper.lessons);
        } else {
            helper = new SelectLessonAndLanguageHelper();
            String directory = getIntent().getStringExtra(DIRECTORY);
            helper.lessons = AppUtils.getLessons(directory);
            if (helper.lessons.length != 0) {
                initializeLessonsSpinner(helper.lessons);
            }
        }
    }

    private void initializeLessonsSpinner(File[] lessons) {

        lessonsAdapter = new LessonsSpinAdapter(SelectLessonAndLanguageActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                lessons);
        spinnerLessons.setAdapter(lessonsAdapter);
        if (helper.isRestore) {
            int position = lessonsAdapter.getPosition(helper.lessonFile);
            spinnerLessons.setSelected(false);  // must
            spinnerLessons.setSelection(position, true);  //must
            initializeLanguageSpinner(helper.lessonItem.getWords());
        }

        spinnerLessons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                File lessonFile = lessonsAdapter.getItem(position);
                helper.lessonFile = lessonFile;
                helper.lessonItem = XmlParser.parseLesson(lessonFile);
                initializeLanguageSpinner(helper.lessonItem.getWords());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void initializeLanguageSpinner(ArrayList<WordItem> words) {

        List<LanguageItem> langs = AppUtils.getLangs(words);
        ;

        langAdapter = new LangSpinAdapter(SelectLessonAndLanguageActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                langs);

        spinnerLang.setAdapter(langAdapter);
        if (helper.isRestore) {
            int position = langAdapter.getPosition(helper.lessonItem.getLanguageItem());
            spinnerLang.setSelected(false);  // must
            spinnerLang.setSelection(position, true);
            helper.isRestore = false;
        }
        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                LanguageItem language = langAdapter.getItem(position);
                helper.lessonItem.setLanguageItem(language);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case com.andreaak.cards.R.id.buttonOk:
                onOkClick();
                break;
            case com.andreaak.cards.R.id.buttonCancel:
                onCancel();
                break;
        }
    }

    private void onOkClick() {
        Intent intent = new Intent();
        intent.putExtra(HELPER, helper);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
