package com.andreaak.cards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.domain.LanguageItem;
import com.andreaak.cards.domain.LessonItem;
import com.andreaak.cards.helpers.CardActivityHelper;
import com.andreaak.cards.helpers.SelectLessonAndLanguageHelper;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.predicates.LessonXmlDirectoryPredicate;
import com.andreaak.cards.utils.SharedPreferencesHelper;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.XmlParser;

public class CardChooseActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_LESSONS_DIRECTORY_CHOOSER = 1;
    private static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 2;
    private static final int REQUEST_PREFERENCES = 4;

    private Menu menu;

     private boolean isPrefChanged;
    ImageButton buttonOpenLastLesson;
    ImageButton buttonOpenLesson;
    ImageButton buttonOpenLessonsFolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Thread.setDefaultUncaughtExceptionHandler(new ActivityExceptionHandler(this));

        setContentView(R.layout.activity_card_choose);

        buttonOpenLastLesson = (ImageButton) findViewById(R.id.buttonOpenLastLesson);
        buttonOpenLastLesson.setOnClickListener(this);
        String lastLesson = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_PATH);
        if (Utils.isEmpty(lastLesson)) {
            buttonOpenLastLesson.setEnabled(false);
        }

        buttonOpenLesson = (ImageButton) findViewById(R.id.buttonOpenLesson);
        buttonOpenLesson.setOnClickListener(this);

        buttonOpenLessonsFolder = (ImageButton) findViewById(R.id.buttonOpenLessonsFolder);
        buttonOpenLessonsFolder.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_choose, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings: {
                isPrefChanged = false;
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_PREFERENCES);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonOpenLastLesson:
                openLastLesson();
                break;
            case R.id.buttonOpenLesson:

                break;
            case R.id.buttonOpenLessonsFolder:
                getLessonsDirectory();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LESSONS_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.PATH);
                    selectLessonAndLanguage(path);
                }
                break;
            case REQUEST_LESSON_AND_LANGUAGE_CHOOSER:
                if (resultCode == RESULT_OK) {
                    SelectLessonAndLanguageHelper selectHelper = (SelectLessonAndLanguageHelper) data.getSerializableExtra(SelectLessonAndLanguageActivity.HELPER);
                    if (selectHelper.lessonItem.isContainsWords()) {
                        openCard(selectHelper.lessonItem);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openLastLesson() {
        String lastLesson = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_PATH);
        String lastLanguage = SharedPreferencesHelper.getInstance().getString(Configs.SP_LAST_LESSON_LANGUAGE);
        LessonItem lessonItem = XmlParser.parseLesson(lastLesson);

        LanguageItem languageItem = LanguageItem.getItem(lastLanguage);
        lessonItem.setLanguageItem(languageItem);
        openCard(lessonItem);
    }

    private void getLessonsDirectory() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new LessonXmlDirectoryPredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_lessons_folder));
        startActivityForResult(intent, REQUEST_LESSONS_DIRECTORY_CHOOSER);
    }

    private void selectLessonAndLanguage(String path) {
        Intent intent = new Intent(this, SelectLessonAndLanguageActivity.class);
        intent.putExtra(SelectLessonAndLanguageActivity.DIRECTORY, path);
        startActivityForResult(intent, REQUEST_LESSON_AND_LANGUAGE_CHOOSER);
    }

    private void openCard(LessonItem lessonItem) {
        CardActivityHelper helper = new CardActivityHelper();
        helper.lessonItem = lessonItem;
        helper.currentWord = helper.lessonItem.getWords().get(0);

        saveLastLesson(lessonItem);

        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra(CardActivity.HELPER, helper);
        startActivity(intent);
    }

    private void saveLastLesson(LessonItem lessonItem) {
        SharedPreferencesHelper.getInstance().save(Configs.SP_LAST_LESSON_PATH, lessonItem.getPath());
        SharedPreferencesHelper.getInstance().save(Configs.SP_LAST_LESSON_LANGUAGE,
                lessonItem.getLanguageItem().toString());
    }
}
