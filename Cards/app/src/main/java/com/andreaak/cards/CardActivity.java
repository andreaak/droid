package com.andreaak.cards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.files.FileItem;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.utils.SharedPreferencesHelper;
import com.andreaak.cards.utils.WordItem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class CardActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String PATH = "Path";

    private File currentDir;
    private Button buttonPrevious;
    private Button buttonNext;
    private TextView textViewWord;
    private TextView textViewTrans;
    private Spinner spinnerLessons;
    private String path;
    private File[] files;
    private String currentLesson;
    private int currentWordIndex;
    private ArrayList<WordItem> words;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        buttonPrevious = (Button) findViewById(R.id.buttonPrevious);
        buttonPrevious.setOnClickListener(this);
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);

        textViewWord = (TextView) findViewById(R.id.textViewWord);
        textViewTrans = (TextView) findViewById(R.id.textViewTrans);

        spinnerLessons = (Spinner)findViewById(R.id.spinnerLessons);

        path = getIntent().getStringExtra(PATH);

        files = GetLessons(path);

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            names.add(files[i].getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, names);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLessons.setAdapter(adapter);
        spinnerLessons.setOnItemSelectedListener(this);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            String savedPath = SharedPreferencesHelper.getInstance().getString(Configs.SP_DOWNLOAD_DIR_PATH);
            currentDir = savedPath.equals("") || !new File(savedPath).exists() ?
                    Environment.getDataDirectory() :
                    new File(savedPath);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
         return currentDir;
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private File[] GetLessons(String path){
        File directory =  new File(path);
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("lesson");
            }
        });
        return files;
    }



//    private void fill(File file) {
//        this.setTitle(file.getAbsolutePath());
//    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        FileItem item = adapter.getItem(position);
//        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
//            currentDir = new File(item.getPath());
//            fill(currentDir);
//        }
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonPrevious:
                //onOkClick(helper.getCurrentDirectory());
                break;
            case R.id.buttonNext:
                //onCancel();
                break;
        }
    }

    private void onOkClick(FileItem item) {
        SharedPreferencesHelper.getInstance().save(Configs.SP_DOWNLOAD_DIR_PATH, item.getPath());

        Intent intent = new Intent();
        intent.putExtra(PATH, item.getPath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
