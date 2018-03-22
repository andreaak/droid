package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.andreaak.cards.R;
import com.andreaak.cards.activitiesShared.HandleExceptionActivity;
import com.andreaak.cards.adapters.LangSpinAdapter;
import com.andreaak.cards.activities.helpers.SelectLanguageHelper;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.XmlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectLanguageActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final String FILE = "File";
    public static final String HELPER = "Helper";

    private Spinner spinnerLang;

    private Button buttonOk;
    private Button buttonCancel;

    private SelectLanguageHelper helper;
    private LangSpinAdapter langAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);

        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        spinnerLang = (Spinner) findViewById(R.id.spinnerLang);

        setTitle(getString(R.string.select_language));

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (SelectLanguageHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            if(helper.lessonItem.isContainsWords()) {
                initializeLanguageSpinner(helper.lessonItem.getWords());
            }
        } else {
            helper = new SelectLanguageHelper();
            String file = getIntent().getStringExtra(FILE);
            helper.lessonItem = XmlParser.parseLesson(new File(file));
            if(helper.lessonItem.isContainsWords()) {
                initializeLanguageSpinner(helper.lessonItem.getWords());
            }
        }
    }

    private void initializeLanguageSpinner(ArrayList<WordItem> words) {

        List<LanguageItem> langs = Utils.getLangs(words.get(0));

        langAdapter = new LangSpinAdapter(SelectLanguageActivity.this,
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
            case R.id.buttonOk:
                onOkClick();
                break;
            case R.id.buttonCancel:
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
