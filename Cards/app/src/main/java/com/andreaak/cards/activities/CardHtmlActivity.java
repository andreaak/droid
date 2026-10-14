package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatDelegate;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.adapters.WordsHtmlSpinAdapter;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CardHtmlActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_UPDATE_WORD = 1;
    public static final String HELPER = "Helper";

    private ImageButton buttonStudy;
    private ImageButton buttonToggle;
    private ImageButton buttonSound;

    private WebView textViewWord1;
    private WebView textViewTrans1;
    private WebView textViewWord2;
    private WebView textViewTrans2;

    private Spinner spinnerWords;

    private CardActivityHelper helper;
    private WordsSpinAdapter wordsAdapter;
    private WordsHtmlSpinAdapter wordsSpinAdapter;
    private VelocityTracker mVelocityTracker = null;
    private float x;
    private boolean isStudy;

    private MediaPlayerHelper mediaHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_html);

        buttonStudy = findViewById(R.id.buttonStudy);
        buttonStudy.setOnClickListener(this);

        buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(this);

        buttonSound = findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);

        textViewWord1 = findViewById(R.id.textViewWord1);
        textViewTrans1 = findViewById(R.id.textViewTrans1);
        textViewWord2 = findViewById(R.id.textViewWord2);
        textViewTrans2 = findViewById(R.id.textViewTrans2);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (CardActivityHelper) getIntent().getSerializableExtra(CardHtmlActivity.HELPER);
            if (helper != null && helper.lessonItem != null) {
                setTitle(helper.lessonItem.getDisplayName());
            }
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
        if (item != null) {
            spinnerWords = (Spinner) item.getActionView();
        }

        if (spinnerWords != null) {
            spinnerWords.setVisibility(View.GONE);
        }

        if (helper != null && helper.lessonItem != null && helper.lessonItem.isContainsWords()) {
            setTitle(helper.lessonItem.getDisplayName());
            helper.lessonItem.resetLanguage();
            wordsAdapter = new WordsSpinAdapter(
                    CardHtmlActivity.this,
                    android.R.layout.simple_spinner_item,
                    helper.lessonItem.getLessonWords(),
                    false,
                    helper.lessonItem.getCurrentLanguage()
            );

            initializeWordsSpinner(helper.lessonItem.getSortedLessonWords(), helper.lessonItem.getCurrentLanguage());

            if (helper.currentWord == null && wordsAdapter.getCount() > 0) {
                helper.currentWord = wordsAdapter.getItem(0);
            }
            if (helper.currentWord != null) {
                activateWord(helper.currentWord);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UPDATE_WORD && resultCode == RESULT_OK && data != null) {
            boolean res = data.getBooleanExtra(EditWordActivity.CHANGED, false);
            if (res) {
                helper.currentWord = (WordItem) data.getSerializableExtra(EditWordActivity.NEWWORD);
                helper.lessonItem.changeWord(helper.currentWord);
                activateWord(helper.currentWord);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_plus || id == R.id.menu_minus || id == R.id.menu_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeWordsSpinner(ArrayList<WordItem> words, String language) {
        wordsSpinAdapter = new WordsHtmlSpinAdapter(
                CardHtmlActivity.this,
                android.R.layout.simple_spinner_item,
                words,
                language
        );

        if (spinnerWords != null) {
            spinnerWords.setAdapter(wordsSpinAdapter);

            if (helper.isRestore) {
                int position = wordsSpinAdapter.getPosition(helper.currentWord);
                spinnerWords.setSelected(false);
                spinnerWords.setSelection(position, true);
                activateWord(helper.currentWord);
                helper.isRestore = false;
            }

            spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.buttonToggle) {
            toggle();
        } else if (id == R.id.buttonSound) {
            playSound();
        } else if (id == R.id.buttonStudy) {
            showStudy();
        }
    }

    private void activateWord(WordItem word) {
        if (isStudy) {
            activateStudyWord(word);
        } else {
            activateCheckWord(word);
        }
    }

    private void activateCheckWord(WordItem word) {
        setWordAndVisibility(textViewWord1, "");
        setTranscriptionAndVisibility(textViewTrans1, "");

        String wordText = word.getValue(helper.lessonItem.getCurrentLanguage());
        setWordAndVisibility(textViewWord2, wordText);

        String transcription = word.getTranscription(helper.lessonItem.getCurrentLanguage());
        String info = word.getInfo(helper.lessonItem.getCurrentLanguage());
        String text = getText(transcription, info);
        setTranscriptionAndVisibility(textViewTrans2, text);

        Queue<String> files = getSoundFiles(helper.lessonItem.getCurrentLanguage());
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void activateStudyWord(WordItem word) {
        LanguageItem languageItem = helper.lessonItem.getLanguageItem();

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        setWordAndVisibility(textViewWord1, wordText);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String text = getText(transcription, info);
        setTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        setWordAndVisibility(textViewWord2, wordText);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        text = getText(transcription, info);
        setTranscriptionAndVisibility(textViewTrans2, text);

        String lang = languageItem.getSoundLanguage();

        Queue<String> files = getSoundFiles(lang);
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private String getText(String transcription, String info) {
        return transcription == null ? info : info == null ? transcription : transcription + "  " + info;
    }

    private void setWordAndVisibility(WebView webView, String word) {
        webView.loadUrl("about:blank");
        if (!Utils.isEmpty(word)) {
            webView.loadData(word, "text/html; charset=UTF-8", null);
        }
    }

    private void setTranscriptionAndVisibility(WebView webView, String transcription) {
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

    private void playSound() {
        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language;
        if (isStudy) {
            LanguageItem languageItem = helper.lessonItem.getLanguageItem();
            language = languageItem.getSecondaryLanguage();
        } else {
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
        Queue<String> files = new ArrayDeque<>();
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
        if (spinnerWords != null) {
            spinnerWords.setSelected(false);
            spinnerWords.setSelection(position, true);
        }
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
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    x = mVelocityTracker.getXVelocity(pointerId);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                x = 0;
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (mediaHelper != null) {
            mediaHelper.stop();
        }
        super.onDestroy();
    }
}
