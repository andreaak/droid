package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.VelocityTracker;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.VerbActivityHelper;
import com.andreaak.cards.adapters.LevelsSpinAdapter;
import com.andreaak.cards.adapters.VerbSpinAdapter;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.utils.Utils;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class IrregularVerbActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_UPDATE_WORD = 1;
    //in
    public static final String HELPER = "Helper";
    public static final String ALL = "All";
    public static final String A1B2 = "A1-B2";

    private TextView textView_1;
    private TextView textView_1_Trans;
    private TextView textView_2;
    private TextView textView_2_Trans;
    private TextView textView_3;
    private TextView textView_3_Trans;
    private TextView textView_4;
    private TextView textView_4_Trans;
    private LinearLayout layout_4;
    private TextView textViewTranslation;

    private ImageButton buttonSound;

    private LinearLayout texts;

    private Spinner spinnerWords;
    private Spinner spinnerLevels;

    private Menu menu;
    private VerbActivityHelper helper;
    private VerbSpinAdapter spinnerAdapterWords;
    private LevelsSpinAdapter spinnerAdapterLevels;
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

        setContentView(R.layout.activity_irregular_verb);

        textView_1 = (TextView) findViewById(R.id.textViewIndefinite);
        textView_1_Trans = (TextView) findViewById(R.id.textViewIndefiniteTrans);
        textView_2 = (TextView) findViewById(R.id.textViewPastSimple);
        textView_2_Trans = (TextView) findViewById(R.id.textViewPastSimpleTrans);
        textView_3 = (TextView) findViewById(R.id.textViewPastParticiple);
        textView_3_Trans = (TextView) findViewById(R.id.textViewPastParticipleTrans);
        textView_4 = (TextView) findViewById(R.id.textView_4);
        textView_4_Trans = (TextView) findViewById(R.id.textView_4_Trans);
        layout_4 = (LinearLayout) findViewById(R.id._4);
        textViewTranslation = (TextView) findViewById(R.id.textViewTranslation);

        buttonSound = (ImageButton) findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonSwitchMode)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonShow)).setOnClickListener(this);

        texts = (LinearLayout) findViewById(R.id.texts);

        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (VerbActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (VerbActivityHelper) getIntent()
                    .getSerializableExtra(IrregularVerbActivity.HELPER);

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
        MenuItem levelsSpinner = menu.findItem(R.id.spinnerLevels);
        spinnerLevels = (android.widget.Spinner) levelsSpinner.getActionView();

        if (helper.lessonItem.isContainsWords()) {

            initializeWordsSpinner(helper.lessonItem.getWords());

            if(!helper.isRestore) {
                helper.currentLevel = spinnerAdapterWords.level;
            }

            if(helper.currentWord == null) {
                helper.currentWord = spinnerAdapterWords.getItem(0);
            }
        }

        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void initializeWordsSpinner(ArrayList<VerbItem> words) {

        ArrayList<VerbItem> copy = (ArrayList<VerbItem>)words.clone();
        spinnerAdapterWords = new VerbSpinAdapter(IrregularVerbActivity.this,
                android.R.layout.simple_spinner_item,
                copy, helper.isSort);

        spinnerWords.setAdapter(spinnerAdapterWords);

        setTitle(words.size() + " " + helper.lessonItem.getDisplayName());
        List<String> levels = GetLevels(words);

        spinnerAdapterLevels = new LevelsSpinAdapter(IrregularVerbActivity.this,
                android.R.layout.simple_spinner_item,
                levels);

        spinnerLevels.setAdapter(spinnerAdapterLevels);

        if (helper.isRestore) {
            int position = spinnerAdapterWords.getPosition(helper.currentWord);
            spinnerWords.setSelected(false);// must
            spinnerWords.setSelection(position, true);  //must

            spinnerAdapterWords.setLevel(helper.currentLevel);

            position = spinnerAdapterLevels.getPosition(helper.currentLevel);
            spinnerLevels.setSelected(false);// must
            spinnerLevels.setSelection(position, true);  //must

            activateWord(helper.currentWord);
            helper.isRestore = false;
        }

        spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                VerbItem word = spinnerAdapterWords.getItem(position);
                helper.currentWord = word;

                activateWord(word);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        //SET LEVEL
        spinnerLevels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                String level = spinnerAdapterLevels.getItem(position);

                if(!spinnerAdapterWords.setLevel(level)) {
                    return;
                }
                setTitle(spinnerAdapterWords.getCount() + " " + helper.lessonItem.getDisplayName());
                helper.currentLevel = level;

                spinnerAdapterWords.setLevel(level);
                spinnerAdapterWords.notifyDataSetChanged();

                spinnerWords.setSelected(false);// must
                spinnerWords.setSelection(0, true);

                helper.currentWord = spinnerAdapterWords.getItem(0);
                activateWord(helper.currentWord);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
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
        } else if(id == R.id.menu_sort)  {
            sortSpinner();
            return true;
        } else if(id == R.id.menu_shuffle_words)  {
            shuffleWords();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonSound)  {
            playSound();
        } else if(id == R.id.buttonSwitchMode) {
            switchHideMode();
        }
        else if(id == R.id.buttonShow) {
            showHiddenForCurrentWord();
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

        float size = textView_1.getTextSize();
        float newSize = size * factor;
        setTextSize(textView_1, newSize);

        size = textView_1_Trans.getTextSize();
        newSize = size * factor;
        setTextSize(textView_1_Trans, newSize);

        size = textView_2.getTextSize();
        newSize = size * factor;
        setTextSize(textView_2, newSize);

        size = textView_2_Trans.getTextSize();
        newSize = size * factor;
        setTextSize(textView_2_Trans, newSize);

        size = textView_3.getTextSize();
        newSize = size * factor;
        setTextSize(textView_3, newSize);

        size = textView_3_Trans.getTextSize();
        newSize = size * factor;
        setTextSize(textView_3_Trans, newSize);

        size = textView_4.getTextSize();
        newSize = size * factor;
        setTextSize(textView_4, newSize);

        size = textView_4_Trans.getTextSize();
        newSize = size * factor;
        setTextSize(textView_4_Trans, newSize);

        //saveFontSize();
    }

    private void setTextSize(TextView textView, float size) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int w = textView.getWidth();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        //textView.setLayoutParams(params);
        textView.setPadding(2, 0, 0, 0);


        textView.setHeight((int) size + 20);

        textView.setWidth(w);
    }

    private void setSettings() {
    }

    private void activateWord(VerbItem word) {

        helper.forceShowCurrentWord = false;

        textView_1.setText(word._1);
        textView_1_Trans.setText(word._1_Trans);
        textView_2.setText(word._2);
        textView_2_Trans.setText(word._2_Trans);
        textView_3.setText(word._3);
        textView_3_Trans.setText(word._3_Trans);

        if(!Utils.isEmpty(word._4)) {
            textView_4.setText(word._4);
            textView_4_Trans.setText(word._4_Trans);
            layout_4.setVisibility(View.VISIBLE);
        } else {
            layout_4.setVisibility(View.GONE);
        }

        String text = Utils.isEmpty(word.level) ? word.translation :
                String.format("%s: %s",  word.level, word.translation);

        textViewTranslation.setText(text);

        //if(helper.hideMode == HideMode.SHOW_ALL) {
            AppUtils.adoptFontSize(textViewTranslation, text, 45, 20);
        //}


        applyHideMode();

        Queue<String> files = getSoundFiles(helper.lessonItem.getLanguage());
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void previousWord() {
        setWord(-1);
    }

    private void nextWord() {
        setWord(1);;
    }

    private void setWord(int index) {
        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        VerbItem word = spinnerAdapterWords.getItem(position + index);
        helper.currentWord = word;
        setTitle(helper.lessonItem.getDisplayName() + " " + (position + index + 1) + " of " + spinnerAdapterWords.getCount());
        position = spinnerAdapterWords.getPosition(helper.currentWord);
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

                if (Math.abs(x) > 500 && spinnerAdapterWords != null) {

                    int position = spinnerAdapterWords.getPosition(helper.currentWord);
                    if (x < 0) {
                        if (position < (spinnerAdapterWords.getCount() - 1)) {
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

    MediaPlayerHelper mediaHelper;

    private void playSound() {

        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        Queue<String> files = getSoundFiles(helper.lessonItem.getLanguage());
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

        List<String> words = AppUtils.getWords(helper.currentWord._1);
        words.addAll(AppUtils.getWords(helper.currentWord._2));
        words.addAll(AppUtils.getWords(helper.currentWord._3));
        if(!Utils.isEmpty(helper.currentWord._4)){
            words.addAll(AppUtils.getWords(helper.currentWord._4));
        }

        for (String word : words) {
            String fileTemplate = AppUtils.getVerbSoundFile(language, word);
            boolean added = AppUtils.addSoundFile(files, fileTemplate);

            if(!added) {
                fileTemplate = AppUtils.getSoundFile(language, word);
                AppUtils.addSoundFile(files, fileTemplate);
            }
        }


        return files;
    }

    private List<String> GetLevels(ArrayList<VerbItem> words) {

        Set<String> s = new TreeSet<>();

        for(VerbItem w : words) {
            if(!Utils.isEmpty(w.getLevel())) {
                s.add(w.getLevel());
            } else {
                s.add("CC");
            }
        }

        List<String> list = new ArrayList<>(s);
        list.add(0, ALL);
        list.add(A1B2);
        return list;
    }

    private void sortSpinner() {
        helper.isSort = !helper.isSort;
        List<VerbItem> filtered = spinnerAdapterWords.setSort(helper.isSort);

        spinnerAdapterWords.clear();

        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, true);
    }

    private void shuffleWords() {
        List<VerbItem> filtered = spinnerAdapterWords.shuffle();

        spinnerAdapterWords.clear();

        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, true);
    }

    private void switchHideMode() {

        helper.hideMode = helper.hideMode.next();

        helper.forceShowCurrentWord = false;

        activateWord(helper.currentWord);
    }

    private void showHiddenForCurrentWord() {

        helper.forceShowCurrentWord = true;

        applyHideMode();
    }

    private void applyHideMode() {

        boolean show2 = true;
        boolean show3 = true;
        boolean show4 = true;
        boolean show5 = true;

        if(!helper.forceShowCurrentWord) {

            switch(helper.hideMode) {

                case HIDE_5:
                    show5 = false;
                    break;

                case HIDE_4_5:
                    show4 = false;
                    show5 = false;
                    break;

                case HIDE_3_4_5:
                    show3 = false;
                    show4 = false;
                    show5 = false;
                    break;
                case HIDE_2_3_4_5:
                    show2 = false;
                    show3 = false;
                    show4 = false;
                    show5 = false;
                    break;

            }
        }

        textView_2.setVisibility(show2 ? View.VISIBLE : View.INVISIBLE);
        textView_2_Trans.setVisibility(show2 ? View.VISIBLE : View.INVISIBLE);

        textView_3.setVisibility(show3 ? View.VISIBLE : View.INVISIBLE);
        textView_3_Trans.setVisibility(show3 ? View.VISIBLE : View.INVISIBLE);

        textView_4.setVisibility(show4 ? View.VISIBLE : View.INVISIBLE);
        textView_4_Trans.setVisibility(show4 ? View.VISIBLE : View.INVISIBLE);
        textView_4.setVisibility(show4 ? View.VISIBLE : View.INVISIBLE);
        textView_4_Trans.setVisibility(show4 ? View.VISIBLE : View.INVISIBLE);

        textViewTranslation.setVisibility(show5 ? View.VISIBLE : View.INVISIBLE);

    }
}

