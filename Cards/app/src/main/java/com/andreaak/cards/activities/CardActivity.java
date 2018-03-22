package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.configs.Configs;
import com.andreaak.cards.configs.SharedPreferencesHelper;
import com.andreaak.cards.helpers.CardActivityHelper;
import com.andreaak.cards.model.WordItem;

import java.util.ArrayList;

public class CardActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_UPDATE_WORD = 1;
    //in
    public static final String HELPER = "Helper";

    private ImageButton buttonToggle;

    private TextView textViewWord;
    private TextView textViewTrans;
    private LinearLayout texts;

    private Spinner spinnerWords;

    private Menu menu;

    private CardActivityHelper helper;
    private WordsSpinAdapter wordsAdapter;
    private VelocityTracker mVelocityTracker = null;
    private float x;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Set the local night mode to some value
            getDelegate().setLocalNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
            // Now recreate for it to take effect
            recreate();
        }

        setContentView(R.layout.activity_card);

        buttonToggle = (ImageButton) findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(this);

        textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewTrans = (TextView) findViewById(R.id.textViewTrans);
        texts = (LinearLayout) findViewById(R.id.texts);

        setFontSize();
        setInitialCardVisibility(false);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (CardActivityHelper) getIntent()
                    .getSerializableExtra(CardActivity.HELPER);

            setTitle(helper.lessonItem.getName());
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        spinnerWords = (android.widget.Spinner) item.getActionView();
        spinnerWords.setVisibility(View.GONE);
        if (helper.lessonItem.isContainsWords()) {
            setTitle(helper.lessonItem.getName());
            helper.lessonItem.resetLanguage();
            initializeWordsSpinner(helper.lessonItem.getWords(), helper.lessonItem.getCurrentLanguage());
        }

        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_UPDATE_WORD:
                if (resultCode == RESULT_OK) {
                    Boolean res = data.getBooleanExtra(EditWordActivity.CHANGED, false);
                    if(res) {
                        helper.currentWord = (WordItem) data.getSerializableExtra(EditWordActivity.NEWWORD);
                        helper.lessonItem.changeWord(helper.currentWord);
                        activateWord(helper.currentWord);
                    }
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case com.andreaak.cards.R.id.menu_plus: {
                textBigger();
                return true;
            }
            case com.andreaak.cards.R.id.menu_minus: {
                textSmaller();
                return true;
            }
            case com.andreaak.cards.R.id.menu_edit_word: {
                editWord();
                return true;
            }
            case com.andreaak.cards.R.id.menu_settings: {
                setSettings();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFontSize() {
        int fontSize = SharedPreferencesHelper.getInstance().getInt(Configs.SP_TEXT_FONT_SIZE);
        if (fontSize > 0) {
            textViewWord.setTextSize(fontSize);
        }
        fontSize = SharedPreferencesHelper.getInstance().getInt(Configs.SP_TRANS_FONT_SIZE);
        if (fontSize > 0) {
            textViewTrans.setTextSize(fontSize);
        }
    }

    private void saveFontSize() {
//        SharedPreferencesHelper.getInstance().save(Configs.SP_TEXT_FONT_SIZE, (int) textViewWord.getTextSize());
//        SharedPreferencesHelper.getInstance().save(Configs.SP_TRANS_FONT_SIZE, (int) textViewTrans.getTextSize());
    }

    private void textSmaller() {

        float factor = 0.95f;
        setTextSize(factor);
    }

    private void textBigger() {
        float factor = 1.05f;
        setTextSize(factor);
    }

    private void setTextSize(float factor) {

        float size = textViewWord.getTextSize();
        float newSize = size * factor;
        setTextSize(textViewWord, newSize);

        size = textViewTrans.getTextSize();
        newSize = size * factor;
        setTextSize(textViewTrans, newSize);

        saveFontSize();
    }

    private void setTextSize(TextView textView, float size) {
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams( android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Math.round(0), Math.round(0), Math.round(0), Math.round(0));

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setLayoutParams(params);
        textView.setPadding(0, 0, 0, 0);
        textView.setHeight((int)size + 20);
    }

    private void editWord() {
        Intent intent = new Intent(this, EditWordActivity.class);
        intent.putExtra(EditWordActivity.LESSON, helper.lessonItem);
        intent.putExtra(EditWordActivity.WORD, helper.currentWord);
        startActivityForResult(intent, REQUEST_UPDATE_WORD);
    }

    private void setSettings() {
    }

    private void initializeWordsSpinner(ArrayList<WordItem> words, String language) {

        wordsAdapter = new WordsSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_item,
                words, language);

        spinnerWords.setAdapter(wordsAdapter);

        setCardVisibility(!words.isEmpty());

        if (helper.isRestore) {
            int position = wordsAdapter.getPosition(helper.currentWord);
            spinnerWords.setSelected(false);// must
            spinnerWords.setSelection(position, true);  //must
            activateWord(helper.currentWord);
            helper.isRestore = false;
        }

        spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                WordItem word = wordsAdapter.getItem(position);
                helper.currentWord = word;
                helper.lessonItem.resetLanguage();

                activateWord(word);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void setInitialCardVisibility(boolean isVisible) {

        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        textViewWord.setVisibility(flag);
        textViewTrans.setVisibility(flag);
        buttonToggle.setVisibility(flag);
        texts.setVisibility(flag);
    }

    private void setCardVisibility(boolean isVisible) {

        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        spinnerWords.setVisibility(flag);
        textViewWord.setVisibility(flag);
        buttonToggle.setVisibility(flag);
        texts.setVisibility(flag);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonToggle:
                Toggle();
                break;
        }
    }

    private void activateWord(WordItem word) {

        textViewWord.setText(word.getValue(helper.lessonItem.getCurrentLanguage()));

        String transcription = word.getTranscription(helper.lessonItem.getCurrentLanguage());
        if (transcription == null) {
            textViewTrans.setVisibility(View.INVISIBLE);
        } else {
            textViewTrans.setVisibility(View.VISIBLE);
            textViewTrans.setText(transcription);
        }
    }

    private void Toggle() {

        helper.lessonItem.ToggleLanguage();

        activateWord(helper.currentWord);
    }

    private void previousWord() {
        int position = wordsAdapter.getPosition(helper.currentWord);
        spinnerWords.setSelection(position - 1);
    }

    private void nextWord() {
        int position = wordsAdapter.getPosition(helper.currentWord);
        spinnerWords.setSelection(position + 1);
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

                x = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                //mVelocityTracker.recycle();

                if (Math.abs(x) > 500 && wordsAdapter != null) {

                    int position = wordsAdapter.getPosition(helper.currentWord);
                    if (x < 0) {
                        if (position < (wordsAdapter.getCount() - 1)) {
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
}
