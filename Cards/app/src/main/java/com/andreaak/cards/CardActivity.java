package com.andreaak.cards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.utils.CardActivityHelper;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.utils.LanguageItem;
import com.andreaak.cards.utils.SelectCardsActivityHelper;
import com.andreaak.cards.utils.SharedPreferencesHelper;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.WordItem;
import com.andreaak.cards.utils.adapters.WordsSpinAdapter;

import java.util.ArrayList;

public class CardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_DIRECTORY_CHOOSER = 1;
    private static final int REQUEST_CARDS_CHOOSER = 2;
    private static final int REQUEST_PREFERENCES = 4;

    private ImageButton buttonToggle;

    private TextView textViewWord;
    private TextView textViewTrans;
    private LinearLayout texts;

    private Spinner spinnerWords;

    private Menu menu;

    private CardActivityHelper helper;
    private WordsSpinAdapter wordsAdapter;
    private boolean isPrefChanged;

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

        //setFontSize();
        setInitialCardVisibility(false);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = new CardActivityHelper();
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
        if (helper.words != null && !helper.words.isEmpty()) {
            setTitle(helper.lessonName);
            helper.currentLanguage = helper.language.getPrimaryLanguage();
            initializeWordsSpinner(helper.words, helper.currentLanguage);
        }

        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.andreaak.cards.R.id.menu_open_folder: {
                getDirectory();
                return true;
            }
            case com.andreaak.cards.R.id.menu_open_file: {

                return true;
            }
            case com.andreaak.cards.R.id.menu_settings: {
                isPrefChanged = false;
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_PREFERENCES);
                return true;
            }

            case com.andreaak.cards.R.id.menu_plus: {
                textBigger();
                return true;
            }

            case com.andreaak.cards.R.id.menu_minus: {
                textSmaller();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.PATH);
                    selectLesson(path);
                }
                break;
            case REQUEST_CARDS_CHOOSER:
                if (resultCode == RESULT_OK) {
                    SelectCardsActivityHelper selectHelper = (SelectCardsActivityHelper) data.getSerializableExtra(SelectCardsActivity.HELPER);
                    if (!selectHelper.words.isEmpty()) {
                        helper.words = selectHelper.words;
                        helper.language = selectHelper.language;
                        helper.currentLanguage = selectHelper.currentLanguage;
                        helper.lessonName = Utils.getFileNameWithoutExtensions(selectHelper.lessonFile.getName());
                        helper.currentWord = helper.words.get(0);
                        setTitle(helper.lessonName);
                        initializeWordsSpinner(helper.words, helper.currentLanguage);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getDirectory() {
        Intent intent1 = new Intent(this, DirectoryChooserActivity.class);
        startActivityForResult(intent1, REQUEST_DIRECTORY_CHOOSER);
    }

    private void selectLesson(String path) {
        Intent intent = new Intent(this, SelectCardsActivity.class);
        intent.putExtra(SelectCardsActivity.DIRECTORY, path);
        startActivityForResult(intent, REQUEST_CARDS_CHOOSER);
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
        SharedPreferencesHelper.getInstance().save(Configs.SP_TEXT_FONT_SIZE, (int) textViewWord.getTextSize());
        SharedPreferencesHelper.getInstance().save(Configs.SP_TRANS_FONT_SIZE, (int) textViewTrans.getTextSize());
    }

    private void textSmaller() {

        float size = textViewWord.getTextSize();
        float newSize = size * 0.95f;
        textViewWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        textViewWord.setHeight((int)(newSize + 10));

        size = textViewTrans.getTextSize();
        newSize = size * 0.95f;
        textViewTrans.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        textViewTrans.setHeight((int)(newSize + 10));
        saveFontSize();
    }

    private void textBigger() {

        float size = textViewWord.getTextSize();
        float newSize = size * 1.05f;
        textViewWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        textViewWord.setHeight((int)(newSize + 10));

        size = textViewTrans.getTextSize();
        newSize = size * 1.05f;
        textViewTrans.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        textViewTrans.setHeight((int)(newSize + 10));

        saveFontSize();
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
                helper.currentLanguage = helper.language.getPrimaryLanguage();
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

        textViewWord.setText(word.getValue(helper.currentLanguage));

        String transcription = word.getTranscription(helper.currentLanguage);
        if (transcription == null) {
            textViewTrans.setVisibility(View.INVISIBLE);
        } else {
            textViewTrans.setVisibility(View.VISIBLE);
            textViewTrans.setText(transcription);
        }
    }

    private void Toggle() {
        LanguageItem lang = helper.language;
        helper.currentLanguage = helper.currentLanguage.equals(lang.getPrimaryLanguage()) ?
                lang.getSecondaryLanguage() :
                lang.getPrimaryLanguage();

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

    private VelocityTracker mVelocityTracker = null;

    float x;

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
                mVelocityTracker.recycle();

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
