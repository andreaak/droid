package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.VerbActivityHelper;
import com.andreaak.cards.adapters.VerbSpinAdapter;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.GoogleItem;
import com.andreaak.common.google.IConnectGoogleDrive;
import com.andreaak.common.google.IGoogleActivity;
import com.andreaak.common.google.IOperationGoogleDrive;
import com.andreaak.common.google.OperationGoogleDrive;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static com.andreaak.common.utils.Utils.showText;

public class VerbActivity extends HandleExceptionAppCompatActivity implements IConnectGoogleDrive,
        IOperationGoogleDrive, IGoogleActivity, View.OnClickListener {

    private static final String SOUND_FORMAT = "mp3";
    private static final String LANGUAGE = "en";

    private static final int REQUEST_UPDATE_WORD = 1;
    private static final int REQUEST_GOOGLE_CONNECT = 2;
    //in
    public static final String HELPER = "Helper";

    private TextView textViewIndefinite;
    private TextView textViewIndefiniteTrans;
    private TextView textViewPastSimple;
    private TextView textViewPastSimpleTrans;
    private TextView textViewPastParticiple;
    private TextView textViewPastParticipleTrans;
    private TextView textViewTranslation;
    private ImageButton buttonSound;

    private LinearLayout texts;

    private Spinner spinnerWords;

    private Menu menu;

    private GoogleDriveHelper googleDriveHelper;
    private OperationGoogleDrive operationGoogleDriveHelper;

    private VerbActivityHelper helper;
    private VerbSpinAdapter wordsAdapter;
    private VelocityTracker mVelocityTracker = null;
    private float x;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Set the local night mode to some value
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            // Now recreate for it to take effect
            recreate();
        }

        setContentView(R.layout.activity_verb);

        textViewIndefinite = (TextView) findViewById(R.id.textViewIndefinite);
        textViewIndefiniteTrans = (TextView) findViewById(R.id.textViewIndefiniteTrans);
        textViewPastSimple = (TextView) findViewById(R.id.textViewPastSimple);
        textViewPastSimpleTrans = (TextView) findViewById(R.id.textViewPastSimpleTrans);
        textViewPastParticiple = (TextView) findViewById(R.id.textViewPastParticiple);
        textViewPastParticipleTrans = (TextView) findViewById(R.id.textViewPastParticipleTrans);
        textViewTranslation = (TextView) findViewById(R.id.textViewTranslation);
        buttonSound = (ImageButton) findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);

        texts = (LinearLayout) findViewById(R.id.texts);

        googleDriveHelper = GoogleDriveHelper.getInstance();
        googleDriveHelper.setActivity(this, this);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (VerbActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (VerbActivityHelper) getIntent()
                    .getSerializableExtra(VerbActivity.HELPER);
        }

        googleDriveHelper = GoogleDriveHelper.getInstance();
        operationGoogleDriveHelper = new OperationGoogleDrive(
                this,
                getString(com.andreaak.cards.R.string.app_name),
                com.andreaak.cards.R.id.groupGoogle);
        googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);
        menu.setGroupVisible(R.id.groupGoogle, googleDriveHelper.isConnected());

        MenuItem item = menu.findItem(R.id.spinner);
        spinnerWords = (Spinner) item.getActionView();
        spinnerWords.setVisibility(View.GONE);
        if (helper.lessonItem.isContainsWords()) {
            initializeWordsSpinner(helper.lessonItem.getWords());
        }

        this.menu = menu;
        operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_UPDATE_WORD:
                if (resultCode == RESULT_OK) {
                    Boolean res = data.getBooleanExtra(EditWordActivity.CHANGED, false);
                    if (res) {
                        helper.currentWord = (VerbItem) data.getSerializableExtra(EditWordActivity.NEWWORD);
                        helper.lessonItem.changeWord(helper.currentWord);
                        activateWord(helper.currentWord);
                    }
                }
                break;
            case REQUEST_GOOGLE_CONNECT:
                operationGoogleDriveHelper.connectGoogleDrive(data, this, googleDriveHelper);
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_plus: {
                textBigger();
                return true;
            }
            case R.id.menu_minus: {
                textSmaller();
                return true;
            }
            case R.id.menu_edit_word: {
                //editWord();
                return true;
            }
            case R.id.menu_select_account: {
                try {
                    startActivityForResult(AccountPicker.newChooseAccountIntent(
                            null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true,
                            null, null, null, null),
                            REQUEST_GOOGLE_CONNECT);
                } catch (Exception ex) {
                    Logger.d(Constants.LOG_TAG, "Google services problem");
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
                return true;
            }
            case R.id.menu_download: {
                //uploadLesson();
                return true;
            }
            case R.id.menu_settings: {
                //setSettings();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonSound:
                playSound();
                break;
        }
    }

    private void textSmaller() {

        float factor = 0.95f;
        setTextSize(factor);
    }

    private void textBigger() {
        float factor = 1.05f;
        setTextSize(factor);
    }

    private void setTextSize(float factor) {

        float size = textViewIndefinite.getTextSize();
        float newSize = size * factor;
        setTextSize(textViewIndefinite, newSize);

        size = textViewIndefiniteTrans.getTextSize();
        newSize = size * factor;
        setTextSize(textViewIndefiniteTrans, newSize);

        size = textViewPastSimple.getTextSize();
        newSize = size * factor;
        setTextSize(textViewPastSimple, newSize);

        size = textViewPastSimpleTrans.getTextSize();
        newSize = size * factor;
        setTextSize(textViewPastSimpleTrans, newSize);

        size = textViewPastParticiple.getTextSize();
        newSize = size * factor;
        setTextSize(textViewPastParticiple, newSize);

        size = textViewPastParticipleTrans.getTextSize();
        newSize = size * factor;
        setTextSize(textViewPastParticipleTrans, newSize);

        //saveFontSize();
    }

    private void setTextSize(TextView textView, float size) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setLayoutParams(params);
        textView.setPadding(0, 0, 0, 0);
        textView.setHeight((int) size + 20);
    }

//    private void editWord() {
//        Intent intent = new Intent(this, EditWordActivity.class);
//        intent.putExtra(EditWordActivity.LESSON, helper.lessonItem);
//        intent.putExtra(EditWordActivity.WORD, helper.currentWord);
//        startActivityForResult(intent, REQUEST_UPDATE_WORD);
//    }

    private void setSettings() {
    }

    private void initializeWordsSpinner(ArrayList<VerbItem> words) {

        wordsAdapter = new VerbSpinAdapter(VerbActivity.this,
                android.R.layout.simple_spinner_item,
                words);

        spinnerWords.setAdapter(wordsAdapter);

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

                VerbItem word = wordsAdapter.getItem(position);
                helper.currentWord = word;

                activateWord(word);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    private void activateWord(VerbItem word) {

        textViewIndefinite.setText(word.infinitive);
        textViewIndefiniteTrans.setText(word.infinitiveTrans);
        textViewPastSimple.setText(word.pastSimple);
        textViewPastSimpleTrans.setText(word.pastSimpleTrans);
        textViewPastParticiple.setText(word.pastParticiple);
        textViewPastParticipleTrans.setText(word.pastParticipleTrans);
        textViewTranslation.setText(word.translation);

        Queue<String> files = getSoundFiles();
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void previousWord() {
        int position = wordsAdapter.getPosition(helper.currentWord);
        spinnerWords.setSelection(position - 1);
    }

    private void nextWord() {
        int position = wordsAdapter.getPosition(helper.currentWord);
        spinnerWords.setSelection(position + 1);
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

                x = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);

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

    @Override
    public void onConnectionOK() {
        menu.setGroupVisible(R.id.groupGoogle, true);
        //setTitle(helper.lessonItem.getDisplayName());
    }

    @Override
    public void onConnectionFail(Exception ex) {
        menu.setGroupVisible(R.id.groupGoogle, false);
        showText(this, R.string.google_error);
        //setTitle(helper.lessonItem.getDisplayName());
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    private void uploadLesson() {
        final boolean[] isDownload = {false};
        final IOperationGoogleDrive act = this;
        setTitle(R.string.search);

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    GoogleItem directory = googleDriveHelper.searchFolder("root", AppConfigs.getInstance().GoogleDir);
                    if (directory != null) {
                        ArrayList<GoogleItem> findFiles = googleDriveHelper.search(directory.getId(),
                                helper.lessonItem.getFileName(), null);
                        for (GoogleItem file : findFiles) {
                            googleDriveHelper.update(file.getId(), null, null, null, new File(helper.lessonItem.getPath()));
                            break;
                        }
                    }
                    publishProgress("Upload Completed");
                    isDownload[0] = true;
                } catch (Exception ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                    return ex;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... strings) {
                super.onProgressUpdate(strings);
                Logger.d(Constants.LOG_TAG, strings[0]);
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    act.onOperationFinished(null);
                } else {
                    act.onOperationFinished(ex);
                }
            }
        }.execute();
    }

    @Override
    public void onOperationProgress(String message) {
        setTitle(message);
    }

    @Override
    public void onOperationFinished(Exception ex) {

        Utils.showText(this, (ex == null) ?
                R.string.upload_success :
                R.string.upload_fault);
    }

    MediaPlayerHelper mediaHelper;

    private void playSound() {

        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        Queue<String> files = getSoundFiles();
        if (files.isEmpty()) {
            return;
        }
        if (mediaHelper == null) {
            mediaHelper = new MediaPlayerHelper();
        }
        mediaHelper.playSound(this, files);
    }

    private Queue<String> getSoundFiles() {

        Queue<String> files = new ArrayDeque<String>();

        List<String> words = AppUtils.getWords(helper.currentWord.infinitive);
        words.addAll(AppUtils.getWords(helper.currentWord.pastSimple));
        words.addAll(AppUtils.getWords(helper.currentWord.pastParticiple));

        for (String word : words) {
            String filePath = AppUtils.getVerbSoundFile(LANGUAGE, word, SOUND_FORMAT);
            File file = new File(filePath);
            if (file.exists()) {
                files.add(filePath);
            }
        }
        return files;
    }

    @Override
    public void onFinished() {

    }
}
