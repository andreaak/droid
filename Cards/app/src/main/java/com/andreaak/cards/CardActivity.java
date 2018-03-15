package com.andreaak.cards;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.utils.LangSpinAdapter;
import com.andreaak.cards.utils.LanguageItem;
import com.andreaak.cards.utils.LessonItem;
import com.andreaak.cards.utils.LessonsSpinAdapter;
import com.andreaak.cards.utils.WordItem;
import com.andreaak.cards.utils.WordsSpinAdapter;
import com.andreaak.cards.utils.XmlParser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CardActivityHelper {
    String path;
    File[] lessons;

    File currentLesson;
    WordItem currentWord;
    ArrayList<WordItem> words;

    LanguageItem language;

    String currentLanguage;
}

public class CardActivity extends Activity implements View.OnClickListener {

    public static final String PATH = "Path";

    private Button buttonPrevious;
    private Button buttonNext;
    private Button buttonToggle;

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
        buttonToggle = (Button) findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(this);

        textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewTrans = (TextView) findViewById(R.id.textViewTrans);

        spinnerLessons = (Spinner)findViewById(R.id.spinnerLessons);
        spinnerLang = (Spinner)findViewById(R.id.spinnerLang);
        spinnerWords = (Spinner)findViewById(R.id.spinnerWords);

        onRestoreNonConfigurationInstance();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastNonConfigurationInstance();
        if (helper != null) {
            initializeLessonsSpinner(helper.lessons);
            //setActiveLesson(helper.currentLesson);
        } else {
            helper = new CardActivityHelper();
            helper.path = getIntent().getStringExtra(PATH);
            helper.lessons = GetLessons(helper.path);
            initializeLessonsSpinner(helper.lessons);
        }
    }

    private void initializeLessonsSpinner(File[] lessons) {

        lessonsAdapter = new LessonsSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                lessons);
        //lessonsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLessons.setAdapter(lessonsAdapter); // Set the custom adapter to the spinner
        // You can create an anonymous listener to handle the event when is selected an spinner item
        spinnerLessons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position
                File lesson = lessonsAdapter.getItem(position);

                //if(helper.currentLesson == null) {
                    helper.currentLesson = lesson;
                    initializeLanguageSpinner(lesson);
                //}
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });
    }

    private void initializeLanguageSpinner(File lessonFile) {

        final LessonItem lesson = XmlParser.parseLesson(lessonFile);

        List<LanguageItem> langs = GetLangs(lesson);

        langAdapter = new LangSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                langs);
        //lessonsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLang.setAdapter(langAdapter); // Set the custom adapter to the spinner
        // You can create an anonymous listener to handle the event when is selected an spinner item
        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position
                LanguageItem language = langAdapter.getItem(position);

                //if(helper.currentLesson == null) {
                helper.language = language;
                helper.currentLanguage = language.getPrimaryLanguage();
                initializeWordsSpinner(lesson, helper.currentLanguage);
                //}
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {

            }
        });
    }

    private List<LanguageItem> GetLangs(LessonItem lesson) {

        List<LanguageItem> langItems = new ArrayList<LanguageItem>();

        WordItem word = lesson.getWords().get(0);
        String[] langs = word.getLangs();
        for(int i = 0; i < langs.length - 1; i++ ) {
            for(int j = i + 1 ; j < word.getLangs().length; j++ ) {
                langItems.add(new LanguageItem(langs[i], langs[j]));
                langItems.add(new LanguageItem(langs[j], langs[i]));
            }
        }

        return langItems;
    }

    private void initializeWordsSpinner(LessonItem lesson, String language) {

        wordsAdapter = new WordsSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_item,
                lesson.getWords(), language);
        //wordsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWords.setAdapter(wordsAdapter); // Set the custom adapter to the spinner
        // You can create an anonymous listener to handle the event when is selected an spinner item
        spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position
                WordItem word = wordsAdapter.getItem(position);
                //if(helper.currentWord == null) {
                    helper.currentWord = word;
                    helper.currentLanguage = helper.language.getPrimaryLanguage();
                    activateWord(word);
                //}

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

    private void activateWord(WordItem word) {

        textViewWord.setText(word.getValue(helper.currentLanguage));

        String transcription = word.getTranscription(helper.currentLanguage);
        if(transcription == null) {
            textViewTrans.setVisibility(View.INVISIBLE);
        } else {
            textViewTrans.setVisibility(View.VISIBLE);
            textViewTrans.setText(transcription);
        }

        int position = wordsAdapter.getPosition(word);
        buttonPrevious.setEnabled(position > 0);
        buttonNext.setEnabled(position < (wordsAdapter.getCount() - 1));
    }

    private File[] GetLessons(String path){
        File directory =  new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
            return s.startsWith("lesson");
            }
        });
        Arrays.sort(files) ;
        return files;
    }

    private void Toggle() {
        LanguageItem lang = helper.language;
        helper.currentLanguage = helper.currentLanguage.equals(lang.getPrimaryLanguage()) ?
                lang.getSecondaryLanguage():
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
}
