package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.EditWordActivityHelper;
import com.andreaak.cards.activitiesShared.HandleExceptionActivity;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.XmlParser;

import java.util.HashMap;

public class EditWordActivity extends HandleExceptionActivity implements View.OnClickListener {

    //in
    public static final String LESSON = "Lesson";
    public static final String WORD = "Word";
    //out
    public static final String CHANGED = "Changed";
    public static final String NEWWORD = "NewWord";

    private Button buttonOk;
    private Button buttonCancel;

    private TextView textViewPrimaryLanguage;
    private EditText editTextPrimaryLanguage;
    private TextView textViewPrimaryTranscription;
    private EditText editTextPrimaryTranscription;

    private TextView textViewSecondaryLanguage;
    private EditText editTextSecondaryLanguage;
    private TextView textViewSecondaryTranscription;
    private EditText editTextSecondaryTranscription;

    private EditWordActivityHelper helper;

    String primaryLanguage;
    String primaryLanguageValue;
    String primaryLanguageTrans;

    String secondaryLanguage;
    String secondaryLanguageValue;
    String secondaryLanguageTrans;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        buttonOk = (Button) findViewById(R.id.buttonSave);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        textViewPrimaryLanguage = (TextView) findViewById(R.id.textViewPrimaryLang);
        editTextPrimaryLanguage = (EditText) findViewById(R.id.editTextPrimaryLang);
        textViewPrimaryTranscription = (TextView) findViewById(R.id.textViewPrimaryTranscription);
        editTextPrimaryTranscription = (EditText) findViewById(R.id.editTextPrimaryTranscription);

        textViewSecondaryLanguage = (TextView) findViewById(R.id.textViewSecondaryLang);
        editTextSecondaryLanguage = (EditText) findViewById(R.id.editTextSecondaryLang);
        textViewSecondaryTranscription = (TextView) findViewById(R.id.textViewSecondaryTranscription);
        editTextSecondaryTranscription = (EditText) findViewById(R.id.editTextSecondaryTranscription);

        setTitle(getString(R.string.edit_word));

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (EditWordActivityHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            helper = new EditWordActivityHelper();
            helper.lessonItem = (LessonItem) getIntent().getSerializableExtra(LESSON);
            helper.wordItem = (WordItem) getIntent().getSerializableExtra(WORD);
            init(helper.lessonItem, helper.wordItem);
        }
        init(helper.lessonItem, helper.wordItem);
    }

    private void init(LessonItem lesson, WordItem wordItem) {

        primaryLanguage = lesson.getLanguageItem().getPrimaryLanguage();
        textViewPrimaryLanguage.setText(primaryLanguage);
        primaryLanguageValue = wordItem.getValue(primaryLanguage);
        editTextPrimaryLanguage.setText(primaryLanguageValue);
        textViewPrimaryTranscription.setText(primaryLanguage + " transcription");
        primaryLanguageTrans = wordItem.getTranscription(primaryLanguage);
        editTextPrimaryTranscription.setText(primaryLanguageTrans);

        secondaryLanguage = lesson.getLanguageItem().getSecondaryLanguage();
        textViewSecondaryLanguage.setText(secondaryLanguage);
        secondaryLanguageValue = wordItem.getValue(secondaryLanguage);
        editTextSecondaryLanguage.setText(secondaryLanguageValue);
        textViewSecondaryTranscription.setText(secondaryLanguage + " transcription");
        secondaryLanguageTrans = wordItem.getTranscription(secondaryLanguage);
        editTextSecondaryTranscription.setText(secondaryLanguageTrans);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonSave:
                onSaveClick();
                break;
            case R.id.buttonCancel:
                onCancel();
                break;
        }
    }

    private void onSaveClick() {
        Intent intent = new Intent();
        boolean res = setData();
        intent.putExtra(CHANGED, res);
        if (res) {
            intent.putExtra(NEWWORD, helper.wordItem);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private boolean setData() {

        HashMap<String, String> map = new HashMap<String, String>();

        String temp = editTextPrimaryLanguage.getText().toString();
        if (!isEqual(primaryLanguageValue, temp)) {
            map.put(primaryLanguage, temp);
            helper.wordItem.addItem(primaryLanguage, temp);
        }

        temp = editTextPrimaryTranscription.getText().toString();
        if (!isEqual(primaryLanguageTrans, temp)) {
            map.put(primaryLanguage + WordItem.TranscriptionSuffix, temp);
            helper.wordItem.addItem(primaryLanguage + WordItem.TranscriptionSuffix, temp);
        }

        temp = editTextSecondaryLanguage.getText().toString();
        if (!isEqual(secondaryLanguageValue, temp)) {
            map.put(secondaryLanguage, temp);
            helper.wordItem.addItem(secondaryLanguage, temp);
        }

        temp = editTextSecondaryTranscription.getText().toString();
        if (!isEqual(secondaryLanguageTrans, temp)) {
            map.put(secondaryLanguage + WordItem.TranscriptionSuffix, temp);
            helper.wordItem.addItem(secondaryLanguage + WordItem.TranscriptionSuffix, temp);
        }

        if (map.isEmpty()) {
            return false;
        }

        boolean res = XmlParser.updateXML(helper.lessonItem.getPath(),
                primaryLanguage, primaryLanguageValue,
                secondaryLanguage, secondaryLanguageValue,
                map);
        return res;
    }

    private boolean isEqual(String value1, String value2) {
        if (Utils.isEmpty(value1)) {
            return Utils.isEmpty(value2);
        }
        return value1.equals(value2);
    }
}
