package com.andreaak.cards.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.VerbActivityHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.VerbLessonItem;
import com.andreaak.cards.predicates.IrregularVerbEnFileNamePredicate;
import com.andreaak.cards.predicates.IrregularVerbEnFilePredicate;
import com.andreaak.cards.predicates.LessonFileNamePredicate;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.FileChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.GoogleFilesChooserActivity;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.Configs;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.google.EmailHolder;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.IOperationGoogleDrive;
import com.andreaak.common.predicates.CompositeFileNamePredicate;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.FileLogger;
import com.andreaak.common.utils.logger.ILogger;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.common.utils.logger.NativeLogger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import static com.andreaak.common.utils.Utils.showText;

public class MainActivity extends HandleExceptionActivity implements IOperationGoogleDrive, View.OnClickListener {

    private static final int REQUEST_GOOGLE_CONNECT = 2;
    private static final int REQUEST_GOOGLE_FILES_CHOOSER = 3;
    private static final int REQUEST_PREFERENCES = 4;
    private static final int REQUEST_VERB_CHOOSER = 5;

    private EmailHolder emailHolder;
    private Menu menu;
    private GoogleDriveHelper helper;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private boolean isPrefChanged;

    private ImageButton buttonOpenCards;
    private ImageButton buttonOpenVerbCards;
    private ImageButton buttonOpenGrammar;

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

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (GoogleDriveHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            SharedPreferencesHelper.initInstance(this);
            GoogleDriveHelper.initInstance(new EmailHolder());
            helper = GoogleDriveHelper.getInstance();
            emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
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
        helper.setActivity(this);
        emailHolder = GoogleDriveHelper.getInstance().getEmailHolder();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.andreaak.cards.R.menu.menu_main, menu);
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, helper.isConnected());
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.andreaak.cards.R.id.menu_select_account: {
                try {
                    startActivityForResult(AccountPicker.newChooseAccountIntent(
                            null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null), REQUEST_GOOGLE_CONNECT);
                } catch (Exception e) {
                    Logger.d(Constants.LOG_TAG, "Google services problem");
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                }
                return true;
            }
            case com.andreaak.cards.R.id.menu_download: {
                getGoogleLessonsFiles();
                return true;
            }
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
                openCards();
                break;
            case R.id.buttonOpenVerbCards:
                getIrregularVerbEnFile();
                break;
            case R.id.buttonOpenGrammar:
                openGrammar();
                break;
        }
    }

    private void openGrammar() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity.PATH, Configs.getInstance().WorkingDir + "/Grammar");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_CONNECT:
                setTitle(com.andreaak.cards.R.string.connecting);
                if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
                    emailHolder.setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                    if (!helper.init()) {
                        showText(this, com.andreaak.cards.R.string.no_google_account);
                        setTitle(com.andreaak.cards.R.string.app_name);
                        Logger.d(Constants.LOG_TAG, getString(com.andreaak.cards.R.string.no_google_account));
                    } else {
                        helper.connect();
                    }
                } else {
                    setTitle(com.andreaak.cards.R.string.app_name);
                }
                break;
            case REQUEST_GOOGLE_FILES_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String[] ids = data.getStringArrayExtra(GoogleFilesChooserActivity.IDS);
                    String[] names = data.getStringArrayExtra(GoogleFilesChooserActivity.NAMES);
                    String path = data.getStringExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH);
                    downloadFromGoogleDrive(ids, names, path);
                }
                break;
            case REQUEST_PREFERENCES:
                if (isPrefChanged) {
                    AppConfigs.getInstance().read();
                    setLogger();
                }
                break;
            case REQUEST_VERB_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra(FileChooserWithButtonsActivity.FILE_PATH);
                    openIrregularVerbEnCards(filePath);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLogger() {
        ILogger log = AppConfigs.getInstance().IsLoggingActive ? new FileLogger() : new NativeLogger();
        Logger.setLogger(log);
    }

    private void openCards() {
        Intent intent = new Intent(this, CardChooseActivity.class);
        startActivity(intent);
    }

    private void getIrregularVerbEnFile() {
        Intent intent = new Intent(this, FileChooserWithButtonsActivity.class);
        intent.putExtra(FileChooserWithButtonsActivity.PREDICATE, new IrregularVerbEnFilePredicate());
        intent.putExtra(FileChooserWithButtonsActivity.TITLE, getString(R.string.select_lesson));
        String initialPath = AppConfigs.getInstance().WorkingDir;
        intent.putExtra(FileChooserWithButtonsActivity.INITIAL_PATH, initialPath);
        startActivityForResult(intent, REQUEST_VERB_CHOOSER);
    }

    private void openIrregularVerbEnCards(String filePath) {
        VerbLessonItem lessonItem = XmlParser.parseVerbLesson(filePath);
        VerbActivityHelper helper = new VerbActivityHelper();
        helper.lessonItem = lessonItem;
        helper.currentWord = helper.lessonItem.getWords().get(0);

        Intent intent = new Intent(this, VerbActivity.class);
        intent.putExtra(CardActivity.HELPER, helper);
        startActivity(intent);
    }

    private void getGoogleLessonsFiles() {
        Intent intent = new Intent(this, GoogleFilesChooserActivity.class);
        CompositeFileNamePredicate predicate = new CompositeFileNamePredicate(
                new LessonFileNamePredicate(), new IrregularVerbEnFileNamePredicate());
        intent.putExtra(GoogleFilesChooserActivity.PREDICATE, predicate);
        intent.putExtra(GoogleFilesChooserActivity.APP_NAME, getString(com.andreaak.cards.R.string.app_name));
        intent.putExtra(GoogleFilesChooserActivity.DOWNLOAD_TO_PATH_INITIAL, AppConfigs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_GOOGLE_FILES_CHOOSER);
    }

    private void downloadFromGoogleDrive(final String[] ids, final String[] names, final String path) {
        if (ids.length == 0) {
            return;
        }

        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false);

        helper.saveFiles(ids, names, path);
    }

    @Override
    public void onConnectionOK() {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, true);
        setTitle(com.andreaak.cards.R.string.app_name);
    }

    @Override
    public void onConnectionFail(Exception ex) {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false);
        showText(this, com.andreaak.cards.R.string.google_error);
        setTitle(com.andreaak.cards.R.string.app_name);
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    @Override
    public void onOperationProgress(String message) {
        setTitle(message);
    }

    @Override
    public void onOperationFinished(Exception ex) {
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, true);
        if (ex == null) {
            showText(this, com.andreaak.cards.R.string.download_success);
        } else {
            showText(this, com.andreaak.cards.R.string.download_fault);
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        setTitle(com.andreaak.cards.R.string.app_name);
    }


}