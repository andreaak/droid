package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.FilesChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.predicates.StudyFilesPredicate;

import java.util.ArrayList;


public class StudyChooseActivity extends HandleExceptionActivity implements View.OnClickListener {

    public static final int REQUEST_LESSON_AND_LANGUAGE_CHOOSER = 1;
    public static final int REQUEST_COMBINE_FILES = 4;
    ImageButton buttonOpenLesson;

    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_study_choose);

        buttonOpenLesson = (ImageButton) findViewById(R.id.buttonOpenLesson);
        buttonOpenLesson.setOnClickListener(this);

        ((ImageButton) findViewById(R.id.buttonCombineFiles)).setOnClickListener(this);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verb_choose, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_COMBINE_FILES:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> files = (ArrayList<String>) data.getSerializableExtra(FilesChooserWithButtonsActivity.FILES);
                    combineFiles(files);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonOpenLesson)  {
            openLesson();
        } else if(id == R.id.buttonCombineFiles)  {
            combineFiles();
        }
    }

    private void combineFiles() {
        Intent intent = new Intent(this, FilesChooserWithButtonsActivity.class);
        intent.putExtra(FilesChooserWithButtonsActivity.PREDICATE, new StudyFilesPredicate());
        intent.putExtra(FilesChooserWithButtonsActivity.TITLE, getString(R.string.combine_files));
        intent.putExtra(FilesChooserWithButtonsActivity.INITIAL_PATH, AppConfigs.getInstance().getStudyDir());
        startActivityForResult(intent, REQUEST_COMBINE_FILES);
    }

    private void combineFiles(ArrayList<String> files) {
        XmlParser.mergeXmlFiles(files);
    }

    private void openLesson() {
        Intent intent = new Intent(this, SelectLessonAndLanguageActivity.class);
        intent.putExtra(SelectLessonAndLanguageActivity.DIRECTORY, AppConfigs.getInstance().getStudyDir());
        intent.putExtra(SelectLessonAndLanguageActivity.PREFIX, "");
        intent.putExtra(SelectLessonAndLanguageActivity.SORT_ITEMS, "true");
        startActivityForResult(intent, REQUEST_LESSON_AND_LANGUAGE_CHOOSER);
    }
}
