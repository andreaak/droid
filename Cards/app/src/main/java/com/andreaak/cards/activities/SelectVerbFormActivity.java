package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.SelectVerbFormHelper;
import com.andreaak.cards.adapters.VerbFormTextViewAdapter;
import com.andreaak.cards.adapters.VerbTypesSpinAdapter;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.VerbFormItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class SelectVerbFormActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final String PATH = "path";

    private Spinner spinnerVerbForm;

    private Button buttonCancel;

    private SelectVerbFormHelper helper;
    private VerbFormTextViewAdapter verbFormsAdapter;
    private VerbTypesSpinAdapter verbTypesAdapter;

    private TextView textView_1;
    private TextView textView_2;
    private TextView textView_3;
    private TextView textView_4;
    private TextView textView_5;
    private TextView textView_6;
    private TextView textView_tr;
    private AutoCompleteTextView autoCompleteTextView;

    private VelocityTracker mVelocityTracker = null;
    private float x;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verbform);

        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        spinnerVerbForm = (Spinner) findViewById(R.id.spinnerVerbForm);

        textView_1 = (TextView) findViewById(R.id.textView_1);
        textView_2 = (TextView) findViewById(R.id.textView_2);
        textView_3 = (TextView) findViewById(R.id.textView_3);
        textView_4 = (TextView) findViewById(R.id.textView_4);
        textView_5 = (TextView) findViewById(R.id.textView_5);
        textView_6 = (TextView) findViewById(R.id.textView_6);
        textView_tr = (TextView) findViewById(R.id.textView_tr);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        setTitle(getString(R.string.select_form));

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (SelectVerbFormHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
            initializeVerbSpinner(helper.verbForms);
        } else {
            try{
                helper = new SelectVerbFormHelper();
                String directory = getIntent().getStringExtra(PATH);
                helper.verbForms = AppUtils.getVerbForms(directory);
                if (helper.verbForms.size() != 0) {
                    initializeVerbSpinner(helper.verbForms);
                }

            } catch(Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }
    }

    private void initializeVerbSpinner(ArrayList<VerbForm> verbForms) {

        verbFormsAdapter = new VerbFormTextViewAdapter(SelectVerbFormActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                verbForms);

        autoCompleteTextView.setAdapter(verbFormsAdapter);

        if (helper.isRestore) {
            int position = verbFormsAdapter.getPosition(helper.verbForm);
            autoCompleteTextView.setSelected(false);  // must
            autoCompleteTextView.setSelection(position);  //must
            initializeVerbFromSpinner(helper.verbForm.getVerbFormItems());
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
                VerbForm verbForm = verbFormsAdapter.getItem(position);
                helper.verbForm = verbForm;
                initializeVerbFromSpinner(helper.verbForm.getVerbFormItems());
            }
        });
    }

    private void initializeVerbFromSpinner(List<VerbFormItem> verbFormItems) {

        verbTypesAdapter = new VerbTypesSpinAdapter(SelectVerbFormActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                verbFormItems);

        spinnerVerbForm.setAdapter(verbTypesAdapter);
        if (helper.isRestore) {
            int position = verbTypesAdapter.getPosition(helper.verbFormItem);
            spinnerVerbForm.setSelected(false);  // must
            spinnerVerbForm.setSelection(position, true);
            helper.isRestore = false;
        }
        spinnerVerbForm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                VerbFormItem item = verbTypesAdapter.getItem(position);
                helper.verbFormItem = item;
                setVerbFormData(helper.verbFormItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void setVerbFormData(VerbFormItem verbFormItem) {

        setText(textView_1, "Ich", verbFormItem.Single1);
        setText(textView_2, "Du", verbFormItem.Single2);
        setText(textView_3, "Er", verbFormItem.Single3);
        setText(textView_4, "Wir", verbFormItem.Plural1);
        setText(textView_5, "Ihr", verbFormItem.Plural2);
        setText(textView_6, "Sie", verbFormItem.Plural3);
        setText(textView_tr, "", verbFormItem.Translation);
    }

    private void setText(TextView textView, String preffix, String item) {
        if(!Utils.isEmpty(item)) {
            textView.setText(preffix + " " + item);
        } else {
            textView.setText("-");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonCancel)  {
            onCancel();
        }
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the
                    // velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.

                x = mVelocityTracker.getXVelocity(pointerId);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                //mVelocityTracker.recycle();

                if (Math.abs(x) > 500 && verbTypesAdapter != null) {

                    int position = verbTypesAdapter.getPosition(helper.verbFormItem);
                    if (x < 0) {
                        if (position < (verbTypesAdapter.getCount() - 1)) {
                            nextWord();
                        }
                    } else {
                        if (position > 0) {
                            previousWord();
                        }
                    }
                }
                x = 0;
                break;
        }
        return true;
    }

    private void previousWord() {
        setWord(-1);
    }

    private void nextWord() {
        setWord(1);
    }

    private void setWord(int index) {

        int position = verbTypesAdapter.getPosition(helper.verbFormItem);
        VerbFormItem item = verbTypesAdapter.getItem(position + index);
        helper.verbFormItem = item;
        setVerbFormData(helper.verbFormItem);
        spinnerVerbForm.setSelected(false);// must
        spinnerVerbForm.setSelection(position + index, false);
    }
}
