package com.andreaak.cards.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.VelocityTracker;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.adapters.WordsHtmlSpinAdapter;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.andreaak.common.utils.Utils.showText;

public class CardHtmlActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_UPDATE_WORD = 1;

    //in
    public static final String HELPER = "Helper";

    private ImageButton buttonStudy;
    private ImageButton buttonToggle;
    private ImageButton buttonSound;

    private WebView textViewWord1;
    private WebView textViewTrans1;
    private WebView textViewWord2;
    private WebView textViewTrans2;
    private LinearLayout texts;

    private Spinner spinnerWords;

    private Menu menu;

//    private GoogleDriveHelper googleDriveHelper;
//    private OperationGoogleDrive operationGoogleDriveHelper;

    private CardActivityHelper helper;
    private WordsSpinAdapter wordsAdapter;
    private WordsHtmlSpinAdapter wordsSpinAdapter;
    private VelocityTracker mVelocityTracker = null;
    private float x;
    private boolean isStudy;

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

        setContentView(R.layout.activity_card_html);

        buttonStudy = (ImageButton) findViewById(R.id.buttonStudy);
        buttonStudy.setOnClickListener(this);

        buttonToggle = (ImageButton) findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(this);

        buttonSound = (ImageButton) findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);

        textViewWord1 = (WebView) findViewById(R.id.textViewWord1);
        textViewTrans1 = (WebView) findViewById(R.id.textViewTrans1);
        textViewWord2 = (WebView) findViewById(R.id.textViewWord2);
        textViewTrans2 = (WebView) findViewById(R.id.textViewTrans2);
        texts = (LinearLayout) findViewById(R.id.texts);

        setFontSize();
        //setInitialCardVisibility();

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (CardActivityHelper) getIntent()
                    .getSerializableExtra(CardHtmlActivity.HELPER);

            setTitle(helper.lessonItem.getDisplayName());
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);

        MenuItem item = menu.findItem(R.id.spinnerWords);
        spinnerWords = (Spinner) item.getActionView();
        spinnerWords.setVisibility(View.GONE);
        if (helper.lessonItem.isContainsWords()) {
            setTitle(helper.lessonItem.getDisplayName());
            helper.lessonItem.resetLanguage();
            wordsAdapter = new WordsSpinAdapter(CardHtmlActivity.this,
                    android.R.layout.simple_spinner_item,
                    helper.lessonItem.getLessonWords(), false, helper.lessonItem.getCurrentLanguage());

            initializeWordsSpinner(helper.lessonItem.getSortedLessonWords(), helper.lessonItem.getCurrentLanguage());

            if(helper.currentWord == null) {
                helper.currentWord = wordsAdapter.getItem(0);
            }
            activateWord(helper.currentWord);
        }

        this.menu = menu;
        //operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_UPDATE_WORD:
                if (resultCode == RESULT_OK) {
                    Boolean res = data.getBooleanExtra(EditWordActivity.CHANGED, false);
                    if (res) {
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

        int id = item.getItemId();
        if(id == R.id.menu_plus)  {
            textBigger();
            return true;
        } else if(id == R.id.menu_minus)  {
            textSmaller();
            return true;
        } else if(id == R.id.menu_settings)  {
            setSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFontSize() {
        float fontSize1 = SharedPreferencesHelper.getInstance().getFloat(AppConfigs.SP_TEXT_FONT_SIZE);
        float fontSize2 = SharedPreferencesHelper.getInstance().getFloat(AppConfigs.SP_TRANS_FONT_SIZE);
        if (fontSize1 > 0 && fontSize2 > 0) {
            //setTextSize(fontSize1, fontSize2, 1);
        }
    }

    private void textSmaller() {

        float factor = 0.95f;
        //setTextSize(textViewWord2.getTextSize(), textViewTrans2.getTextSize(), factor);
    }

    private void textBigger() {
        float factor = 1.05f;
        //setTextSize(textViewWord2.getTextSize(), textViewTrans2.getTextSize(), factor);
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

        wordsSpinAdapter = new WordsHtmlSpinAdapter(CardHtmlActivity.this,
                android.R.layout.simple_spinner_item,
                words, language);

        spinnerWords.setAdapter(wordsSpinAdapter);

        if (helper.isRestore) {
            int position = wordsSpinAdapter.getPosition(helper.currentWord);
            spinnerWords.setSelected(false);// must
            spinnerWords.setSelection(position, true);  //must
            activateWord(helper.currentWord);
            helper.isRestore = false;
        }

        spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                WordItem word = wordsSpinAdapter.getItem(position);
                helper.currentWord = word;
                helper.lessonItem.resetLanguage();

                activateWord(word);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonToggle)  {
            toggle();
        } else if(id == R.id.buttonSound)  {
            playSound();
        } else if(id == R.id.buttonStudy) {
            showStudy();
        }
    }

    private void activateWord(WordItem word) {
        if(isStudy) {
            activateStudyWord(word);
        } else {
            activateCheckWord(word);
        }
    }

    private void activateCheckWord(WordItem word) {

        SetWordAndVisibility(textViewWord1, "");
        SetTranscriptionAndVisibility(textViewTrans1, "");

        String wordText = word.getValue(helper.lessonItem.getCurrentLanguage());
        SetWordAndVisibility(textViewWord2, wordText);

        String transcription = word.getTranscription(helper.lessonItem.getCurrentLanguage());
        String info = word.getInfo(helper.lessonItem.getCurrentLanguage());
        String text = getText(transcription, info);
        SetTranscriptionAndVisibility(textViewTrans2, text);

        Queue<String> files = getSoundFiles(helper.lessonItem.getCurrentLanguage());
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void activateStudyWord(WordItem word) {

        LanguageItem languageItem = helper.lessonItem.getLanguageItem();

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        SetWordAndVisibility(textViewWord1, wordText);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String text = getText(transcription, info);
        SetTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        SetWordAndVisibility(textViewWord2, wordText);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        text = getText(transcription, info);
        SetTranscriptionAndVisibility(textViewTrans2, text);

        String lang = languageItem.getSoundLanguage();

        Queue<String> files = getSoundFiles(lang);
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private String getText(String transcription, String info) {
        return transcription == null ? info : info == null ? transcription : transcription + "  " + info;
    }

    private void SetWordAndVisibility(WebView webView, String word) {
        webView.loadUrl("about:blank");
        if (!Utils.isEmpty(word)) {
            webView.loadData(word, "text/html; charset=UTF-8", null);
        }
    }

    private void SetTranscriptionAndVisibility(WebView webView, String transcription) {

        webView.loadUrl("about:blank");
        if (!Utils.isEmpty(transcription)) {
            webView.loadData(transcription, "text/html; charset=UTF-8", null);

        }
    }

    private void toggle() {
        helper.lessonItem.ToggleLanguage();
        activateWord(helper.currentWord);
    }

    private void showStudy() {
        isStudy = !isStudy;
        activateWord(helper.currentWord);
    }

    MediaPlayerHelper mediaHelper;

    private void playSound() {

        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language;
        if(isStudy){
            LanguageItem languageItem = helper.lessonItem.getLanguageItem();
            language = languageItem.getSecondaryLanguage();
        }else {
            language = helper.lessonItem.getCurrentLanguage();
        }

        Queue<String> files = getSoundFiles(language);
        if (files.isEmpty()) {
            return;
        }
        if (mediaHelper == null) {
            mediaHelper = new MediaPlayerHelper();
        }
        mediaHelper.playSound(this, files);
    }

    private Queue<String> getSoundFiles(String language) {

        Queue<String> files = new ArrayDeque<String>();

        String t = Html.fromHtml(helper.currentWord.getValue(language)).toString();

        List<String> words = AppUtils.getWords(t);

        for (String word : words) {
            String fileTemplate = AppUtils.getSoundFile(language, word);
            AppUtils.addSoundFile(files, fileTemplate);
        }
        return files;
    }

    private void previousWord() {
        setWord(-1);
    }

    private void nextWord() {
        setWord(1);
    }

    private void setWord(int index) {
        int position = wordsAdapter.getPosition(helper.currentWord);
        WordItem word = wordsAdapter.getItem(position + index);
        helper.currentWord = word;

        position = wordsSpinAdapter.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, true);
        activateWord(word);
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