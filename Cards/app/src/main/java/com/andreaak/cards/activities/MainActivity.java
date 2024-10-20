package com.andreaak.cards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.google.EmailHolder;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.FileLogger;
import com.andreaak.common.utils.logger.ILogger;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.common.utils.logger.NativeLogger;

public class MainActivity extends HandleExceptionActivity implements View.OnClickListener {

    private static final int REQUEST_PREFERENCES = 4;

    private GoogleDriveHelper googleDriveHelper;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private boolean isPrefChanged;

    private ImageButton buttonOpenCards;
    private ImageButton buttonOpenVerbCards;
    private ImageButton buttonOpenGrammar;
    private ImageButton buttonOpenVerbForm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.andreaak.cards.R.layout.activity_main);

        buttonOpenCards = (ImageButton) findViewById(R.id.buttonOpenCards);
        buttonOpenCards.setOnClickListener(this);

        buttonOpenVerbCards = (ImageButton) findViewById(R.id.buttonOpenVerbCards);
        buttonOpenVerbCards.setOnClickListener(this);

        buttonOpenGrammar = (ImageButton) findViewById(R.id.buttonOpenGrammar);
        buttonOpenGrammar.setOnClickListener(this);

        buttonOpenVerbForm = (ImageButton) findViewById(R.id.buttonOpenVerbForm);
        buttonOpenVerbForm.setOnClickListener(this);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        googleDriveHelper = (GoogleDriveHelper) getLastNonConfigurationInstance();
        if (googleDriveHelper == null) {
            SharedPreferencesHelper.initInstance(this);
            GoogleDriveHelper.initInstance(new EmailHolder());
            googleDriveHelper = GoogleDriveHelper.getInstance();
            Utils.init(this);
            AppConfigs.getInstance().init(this);
            AppConfigs.getInstance().read();
            setLogger();
            prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    isPrefChanged = true;
                }
            };

            SharedPreferencesHelper.getInstance().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(prefListener);

        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return googleDriveHelper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.andreaak.cards.R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.andreaak.cards.R.id.menu_exit: {
                finish();
                return true;
            }
            case com.andreaak.cards.R.id.menu_settings: {
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
            case R.id.buttonOpenCards:
                chooseLesson();
                break;
            case R.id.buttonOpenVerbCards:
                chooseIrregularVerbs();
                break;
            case R.id.buttonOpenGrammar:
                openGrammar();
                break;
            case R.id.buttonOpenVerbForm:
                openVerbForm();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PREFERENCES:
                if (isPrefChanged) {
                    AppConfigs.getInstance().read();
                    setLogger();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLogger() {
        ILogger log = AppConfigs.getInstance().IsLoggingActive ? new FileLogger() : new NativeLogger();
        Logger.setLogger(log);
    }

    private void chooseLesson() {
        Intent intent = new Intent(this, LessonChooseActivity.class);
        startActivity(intent);
    }

    private void chooseIrregularVerbs() {
        Intent intent = new Intent(this, IrregularVerbChooseActivity.class);
        intent.putExtra(IrregularVerbChooseActivity.PATH, AppConfigs.getInstance().getIrregularVerbDir());
        startActivity(intent);
    }

    private void openGrammar() {
        Intent intent = new Intent(this, GrammarChooseActivity.class);
        intent.putExtra(GrammarChooseActivity.PATH, AppConfigs.getInstance().getGrammarDir());
        startActivity(intent);
    }

    private void openVerbForm() {
        Intent intent = new Intent(this, SelectVerbFormActivity.class);
        intent.putExtra(SelectVerbFormActivity.PATH, AppConfigs.getInstance().getVerbFormDir());
        startActivity(intent);
    }
}

