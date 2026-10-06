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
import com.andreaak.cards.adapters.SearchRuTextViewAdapter;
import com.andreaak.cards.adapters.SearchTextViewAdapter;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.util.ArrayList;

public class SearchRuActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final String PATH = "path";
    public static final String PREFIXES = "prefixes";

    private Button buttonOk;
    private Button buttonCancel;
    private Button buttonClear;

    private SelectSimpleWordHelper helper;
    private SearchRuTextViewAdapter verbFormsAdapter;

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
        autoCompleteTextView.setThreshold(2);

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
            if(helper.items != null) {

                initializeVerbSpinner((ArrayList<SimpleWordItem>) helper.items.clone(), helper.lg.getSecondaryLanguage(), helper.lg.getPrimaryLanguage());
                setTitle(String.valueOf(helper.items.size()));
            }
        } else {
            try{
                helper = new SelectSimpleWordHelper();
                String directories = getIntent().getStringExtra(PATH);
                String prefixes = getIntent().getStringExtra(PREFIXES);
                String lang1 = "ru";
                String lang2 = "de";
                if(directories.toLowerCase().contains("english")) {
                    lang2 = "en";
                }
                helper.lg = new LanguageItem(lang2, lang1);

                setTitle("Download...");
                new Thread(() -> {

                    ArrayList<SimpleWordItem> items =
                            AppUtils.getSimpleWortItems(directories, prefixes);
                    helper.items = items;

                    runOnUiThread(() -> {

                        initializeVerbSpinner((ArrayList<SimpleWordItem>) helper.items.clone(),
                                helper.lg.getSecondaryLanguage(),
                                helper.lg.getPrimaryLanguage());
                        setTitle(String.valueOf(helper.items.size()));
                    });

                }).start();

            } catch(Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
    }

    private void initializeVerbSpinner(ArrayList<SimpleWordItem> items, String lang1, String lang2) {

        //ArrayList<SimpleWordItem> list = (ArrayList<SimpleWordItem>) items.clone();
        verbFormsAdapter = new SearchRuTextViewAdapter(SearchRuActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                items, lang1, lang2);

        autoCompleteTextView.setAdapter(verbFormsAdapter);



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
                SimpleWordItem word = verbFormsAdapter.getItem(position);
                helper.currentItem = word;
            }
        });

        if (helper.isRestore) {
            if(helper.currentItem != null) {
                autoCompleteTextView.setText(helper.currentItem.getRuDisplayName(lang1, lang2), false);
            }
        } else {
            autoCompleteTextView.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonOk)  {
            onOkClick();
        } else if(id == R.id.buttonCancel)  {
            onCancel();
        } else if(id == R.id.buttonClear)  {
            onClear();
        }
    }

    private void onOkClick() {

        if (helper.currentItem != null) {
            SimpleCardActivityHelper hp = new SimpleCardActivityHelper();
            hp.currentSimpleWord = helper.currentItem;
            hp.language = helper.lg;
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
