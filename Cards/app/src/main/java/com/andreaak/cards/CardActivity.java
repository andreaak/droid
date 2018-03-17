package com.andreaak.cards;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.utils.CardActivityHelper;
import com.andreaak.cards.utils.LanguageItem;
import com.andreaak.cards.utils.LessonItem;
import com.andreaak.cards.utils.WordItem;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.cards.utils.adapters.LangSpinAdapter;
import com.andreaak.cards.utils.adapters.LessonsSpinAdapter;
import com.andreaak.cards.utils.adapters.WordsSpinAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardActivity extends Activity implements View.OnClickListener {

    public static final String PATH = "Path";

    private Button buttonPrevious;
    private Button buttonNext;
    private ImageButton buttonToggle;

    private TextView textViewWord;
    private TextView textViewTrans;

    private Spinner spinnerLessons;
    private Spinner spinnerLang;
    private Spinner spinnerWords;

    private CardActivityHelper helper;
    private LessonsSpinAdapter lessonsAdapter;
    private LangSpinAdapter langAdapter;
    private WordsSpinAdapter wordsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        buttonPrevious = (Button) findViewById(R.id.buttonPrevious);
        buttonPrevious.setOnClickListener(this);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);
        buttonToggle = (ImageButton) findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(this);

        textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewTrans = (TextView) findViewById(R.id.textViewTrans);

        spinnerLessons = (Spinner) findViewById(R.id.spinnerLessons);
        spinnerLang = (Spinner) findViewById(R.id.spinnerLang);
        spinnerWords = (Spinner) findViewById(R.id.spinnerWords);

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
            initializeLessonsSpinner(helper.lessons);
        } else {
            helper = new CardActivityHelper();
            helper.lessons = GetLessons(getIntent().getStringExtra(PATH));
            initializeLessonsSpinner(helper.lessons);
        }
    }

    private void initializeLessonsSpinner(File[] lessons) {

        lessonsAdapter = new LessonsSpinAdapter(CardActivity.this,
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

        langAdapter = new LangSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                langs);

        spinnerLang.setAdapter(langAdapter);
        if (helper.isRestore) {
            int position = langAdapter.getPosition(helper.language);
            spinnerLang.setSelected(false);  // must
            spinnerLang.setSelection(position, true);  //must
            initializeWordsSpinner(helper.words, helper.currentLanguage);
        }
        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                LanguageItem language = langAdapter.getItem(position);
                helper.language = language;
                helper.currentLanguage = language.getPrimaryLanguage();
                initializeWordsSpinner(helper.words, helper.currentLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void initializeWordsSpinner(ArrayList<WordItem> words, String language) {

        wordsAdapter = new WordsSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_item,
                words, language);

        spinnerWords.setAdapter(wordsAdapter);
        if (helper.isRestore) {
            int position = wordsAdapter.getPosition(helper.currentWord);
            spinnerWords.setSelected(false);// must
            spinnerWords.setSelection(position, true);  //must
            activateWord(helper.currentWord);
            helper.isRestore = false;
        }


        int flag = words.isEmpty() ? View.INVISIBLE : View.VISIBLE;

        textViewWord.setVisibility(flag);
        textViewTrans.setVisibility(flag);
        buttonToggle.setVisibility(flag);
        buttonNext.setVisibility(flag);
        buttonPrevious.setVisibility(flag);

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonPrevious:
                previousWord();
                break;
            case R.id.buttonNext:
                nextWord();
                break;
            case R.id.buttonToggle:
                Toggle();
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

    private void activateLanguageItem(LanguageItem lang) {
        int position = langAdapter.getPosition(lang);
        spinnerLang.setSelection(position);
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

        int position = wordsAdapter.getPosition(word);
        buttonPrevious.setEnabled(position > 0);
        buttonNext.setEnabled(position < (wordsAdapter.getCount() - 1));
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

                if (Math.abs(x) > 500) {

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
