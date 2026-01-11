package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.SelectSimpleWordHelper;
import com.andreaak.cards.activities.helpers.SimpleCardActivityHelper;
import com.andreaak.cards.adapters.SearchTextViewAdapter;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.util.ArrayList;

public class SearchActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final String PATH = "path";
    public static final String PREFIXES = "prefixes";

    private Button buttonOk;
    private Button buttonCancel;
    private Button buttonClear;

    private SelectSimpleWordHelper helper;
    private SearchTextViewAdapter verbFormsAdapter;
    LanguageItem lg;

    private AutoCompleteTextView autoCompleteTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);

        buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(this);

        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        setTitle(getString(R.string.select_form));
        lg = new LanguageItem("de", "ru");
        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (SelectSimpleWordHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
            initializeVerbSpinner(helper.items);
        } else {
            try{
                helper = new SelectSimpleWordHelper();
                String directories = getIntent().getStringExtra(PATH);
                String prefixes = getIntent().getStringExtra(PREFIXES);
                helper.items = AppUtils.getSimpleWortItems(directories, prefixes, lg);
                if (helper.items.size() != 0) {
                    setTitle(String.valueOf(helper.items.size()));
                    initializeVerbSpinner(helper.items);
                }

            } catch(Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
    }

    private void initializeVerbSpinner(ArrayList<WordItem> verbForms) {

        verbFormsAdapter = new SearchTextViewAdapter(SearchActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                verbForms, "de");

        autoCompleteTextView.setAdapter(verbFormsAdapter);

        if (helper.isRestore) {
            int position = verbFormsAdapter.getPosition(helper.currentItem);
            autoCompleteTextView.setSelected(false);  // must
            autoCompleteTextView.setSelection(position);  //must
        }

        autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                WordItem verbForm = verbFormsAdapter.getItem(position);
                helper.currentItem = verbForm;
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
             case R.id.buttonClear:
                 onClear();
                break;
        }
    }

    private void onOkClick() {

        if (helper.currentItem != null) {
            SimpleCardActivityHelper hp = new SimpleCardActivityHelper();
            hp.currentSimpleWord = helper.currentItem;
            hp.language = lg;
            Intent intent = new Intent(this, SimpleCardActivity.class);
            intent.putExtra(SimpleCardActivity.HELPER, hp);
            startActivity(intent);
        }
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void onClear() {
        autoCompleteTextView.setText("");
    }
}
