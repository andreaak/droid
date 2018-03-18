package com.andreaak.cards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.andreaak.cards.utils.LanguageItem;
import com.andreaak.cards.utils.LessonItem;
import com.andreaak.cards.utils.SelectCardsActivityHelper;
import com.andreaak.cards.utils.WordItem;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.cards.utils.adapters.LangSpinAdapter;
import com.andreaak.cards.utils.adapters.LessonsSpinAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectCardsActivity extends Activity implements View.OnClickListener {

    public static final String DIRECTORY = "Path";
    public static final String HELPER = "Helper";

    private Spinner spinnerLessons;
    private Spinner spinnerLang;

    private Button buttonOk;
    private Button buttonCancel;

    private SelectCardsActivityHelper helper;
    private LessonsSpinAdapter lessonsAdapter;
    private LangSpinAdapter langAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cards);

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
        helper = (SelectCardsActivityHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
            initializeLessonsSpinner(helper.lessons);
        } else {
            helper = new SelectCardsActivityHelper();
            String directory = getIntent().getStringExtra(DIRECTORY);
            helper.lessons = GetLessons(directory);
            if(helper.lessons.length != 0) {
                initializeLessonsSpinner(helper.lessons);
            }
        }
    }

    private void initializeLessonsSpinner(File[] lessons) {

        lessonsAdapter = new LessonsSpinAdapter(SelectCardsActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                lessons);
        spinnerLessons.setAdapter(lessonsAdapter);
        if (helper.isRestore) {
            int position = lessonsAdapter.getPosition(helper.lessonFile);
            spinnerLessons.setSelected(false);  // must
            spinnerLessons.setSelection(position, true);  //must
            initializeLanguageSpinner(helper.words);
        }

        spinnerLessons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                File lessonFile = lessonsAdapter.getItem(position);
                helper.lessonFile = lessonFile;
                LessonItem lesson = XmlParser.parseLesson(lessonFile);
                helper.words = lesson.getWords();
                initializeLanguageSpinner(lesson.getWords());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void initializeLanguageSpinner(ArrayList<WordItem> words) {

        List<LanguageItem> langs = GetLangs(words.get(0));

        langAdapter = new LangSpinAdapter(SelectCardsActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                langs);

        spinnerLang.setAdapter(langAdapter);
        if (helper.isRestore) {
            int position = langAdapter.getPosition(helper.language);
            spinnerLang.setSelected(false);  // must
            spinnerLang.setSelection(position, true);
            helper.isRestore = false;
        }
        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                LanguageItem language = langAdapter.getItem(position);
                helper.language = language;
                helper.currentLanguage = language.getPrimaryLanguage();
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

    private List<LanguageItem> GetLangs(WordItem word) {

        List<LanguageItem> langItems = new ArrayList<LanguageItem>();

        String[] langs = word.getLangs();
        for (int i = 0; i < langs.length - 1; i++) {
            for (int j = i + 1; j < word.getLangs().length; j++) {
                langItems.add(new LanguageItem(langs[i], langs[j]));
                langItems.add(new LanguageItem(langs[j], langs[i]));
            }
        }

        return langItems;
    }

    private File[] GetLessons(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });
        Arrays.sort(files);
        return files;
    }

    private void onOkClick() {
        Intent intent = new Intent();
        intent.putExtra(HELPER, (Serializable) helper);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
