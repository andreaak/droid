package com.andreaak.cards.activities;

import android.os.Bundle;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.SimpleCardActivityHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class SimpleCardActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    //in
    public static final String HELPER = "Helper";

    private ImageButton buttonSound;
    private ImageButton buttonExample;
    private ImageButton buttonDescription;
    private ImageButton buttonGPTDescription;
    private ImageButton buttonPrap;

    private TextView textViewWord1;
    private TextView textViewTrans1;
    private TextView textViewWord2;
    private TextView textViewTrans2;
    private TextView textViewExample;

    private SimpleCardActivityHelper helper;

    private VelocityTracker mVelocityTracker = null;
    private float x;
    private boolean isExample;
    private boolean isDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Set the local night mode to some value
            getDelegate().setLocalNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
            // Now recreate for it to take effect
            recreate();
        }

        setContentView(R.layout.activity_card);

        findViewById(R.id.buttonStudy).setVisibility(View.INVISIBLE);
        findViewById(R.id.buttonToggle).setVisibility(View.INVISIBLE);
        findViewById(R.id.buttonRemove).setVisibility(View.INVISIBLE);

        buttonSound = (ImageButton) findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);

        buttonExample = (ImageButton) findViewById(R.id.buttonExample);
        buttonExample.setOnClickListener(this);

        buttonDescription = (ImageButton) findViewById(R.id.buttonDescription);
        buttonDescription.setOnClickListener(this);

        buttonGPTDescription = (ImageButton) findViewById(R.id.buttonGPTDescription);
        buttonGPTDescription.setOnClickListener(this);

        buttonPrap = (ImageButton) findViewById(R.id.buttonPrap);
        buttonPrap.setOnClickListener(this);

        textViewWord1 = (TextView) findViewById(R.id.textViewWord1);
        textViewTrans1 = (TextView) findViewById(R.id.textViewTrans1);
        textViewWord2 = (TextView) findViewById(R.id.textViewWord2);
        textViewTrans2 = (TextView) findViewById(R.id.textViewTrans2);
        textViewExample = (TextView) findViewById(R.id.textViewExample);

        setFontSize();
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {

        helper = (SimpleCardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper == null) {
            helper =  (SimpleCardActivityHelper) getIntent().getSerializableExtra(SimpleCardActivity.HELPER);
            setTitle(helper.currentSimpleWord.getDisplayName(helper.language.getPrimaryLanguage()));
        }
        String lang = helper.language.getPrimaryLanguage();
        WordItem res = XmlParser.getWordItem(helper.currentSimpleWord, lang);

        if(res != null) {
            helper.currentWord = res;
            activateWord(res);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    private void setFontSize() {
        float factor = SharedPreferencesHelper.getInstance().getFloat(AppConfigs.SP_TEXT_FONT_SIZE);
        setTextSize(factor);
    }

    private void saveFontSize(float factor) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_TEXT_FONT_SIZE, factor);
    }

    private void setTextSize(float factor) {
        setTextSize(textViewWord1, factor);
        setTextSize(textViewTrans1, factor);
        setTextSize(textViewWord2, factor);
        setTextSize(textViewTrans2, factor);
        setTextSize(textViewExample, factor);
        saveFontSize(factor);
    }

    private void setTextSize(TextView view, float factor) {
        float newSize = view.getTextSize() * factor;
        setViewTextSize(view, newSize);
    }

    private void setViewTextSize(TextView textView, float size) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Math.round(0), Math.round(0), Math.round(0), Math.round(0));

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        textView.setPadding(0, 0, 0, 0);
        //textView.setHeight((int) size + 20);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonSound:
                playSound();
                break;

            case R.id.buttonExample:
                showExample();
                break;
            case R.id.buttonDescription:
                showDescription(v);
                break;
            case R.id.buttonGPTDescription:
                showGPTDescription(v);
                break;
            case R.id.buttonPrap:
                showPrap(v);
                break;
        }
    }

    private void activateWord(WordItem word) {

        LanguageItem languageItem = helper.language;

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        SetWordAndVisibility(textViewWord1, wordText);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String level = word.getLevel(languageItem.getPrimaryLanguage());
        String text = combineText(combineText(transcription, info), level);
        SetTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        SetWordAndVisibility(textViewWord2, wordText);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        text = combineText(transcription, info);
        SetTranscriptionAndVisibility(textViewTrans2, text);

        String example = word.getExample(languageItem.getPrimaryLanguage());
        if(Utils.isEmpty(example)) {
            example = word.getExample(languageItem.getSecondaryLanguage());
        }
        int flag = !Utils.isEmpty(example) ? View.VISIBLE : View.INVISIBLE;
        buttonExample.setVisibility(flag);

        String description = getDescription(word);
        flag = !Utils.isEmpty(description) ? View.VISIBLE : View.INVISIBLE;
        buttonDescription.setVisibility(flag);

        description = getGPTDescription(word);
        flag = !Utils.isEmpty(description) ? View.VISIBLE : View.INVISIBLE;
        buttonGPTDescription.setVisibility(flag);

        String prap = getPrap(word);
        flag = !Utils.isEmpty(prap) ? View.VISIBLE : View.INVISIBLE;
        buttonPrap.setVisibility(flag);

        if(isExample && !Utils.isEmpty(example)) {
            textViewExample.setGravity(Gravity.CENTER);
            SetTranscriptionAndVisibility(textViewExample, example);
        } else if(isDescription && !Utils.isEmpty(description)) {
            textViewExample.setGravity(Gravity.LEFT);
            SetTranscriptionAndVisibility(textViewExample, description);
        } else {
            setVisibility(textViewExample, View.GONE);
        }

        Queue<String> files = getSoundFiles(languageItem.getSoundLanguage());
        boolean isVisible = !files.isEmpty();
        flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private String combineText(String first, String second) {
        return first == null ? second : second == null ? first : first + "  " + second;
    }

    private void SetWordAndVisibility(TextView textView, String word) {
        if (!Utils.isEmpty(word)) {
            textView.setText(word);
            if(word.toLowerCase().startsWith("der ")) {
                textView.setTextColor(getResources().getColor(R.color.colorBlue));
            } else if(word.toLowerCase().startsWith("die ")) {
                textView.setTextColor(getResources().getColor(R.color.colorRed));
            } else if(word.toLowerCase().startsWith("das ")) {
                textView.setTextColor(getResources().getColor(R.color.colorGreen));
            } else {
                //textView.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        }
        setVisibility(textView, View.VISIBLE);
    }

    private void SetTranscriptionAndVisibility(TextView textView, String transcription) {
        if (!Utils.isEmpty(transcription)) {
            textView.setText(transcription);
            setVisibility(textView, View.VISIBLE);
        } else {
            setVisibility(textView, View.GONE);
        }
    }

    private void setVisibility(TextView textView, int flag){
        if(textView.getVisibility() != flag){
            textView.setVisibility(flag);
        }
    }

    private void showExample() {
        isExample = !isExample;
        isDescription = false;
        activateWord(helper.currentWord);
    }

    PopupWindow popupWindow;
    View popupView;


    private void showDescription(View v) {

        String info = getDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getDescription(WordItem word) {
        String info = word.getDescription(helper.language.getPrimaryLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getDescription(helper.language.getSecondaryLanguage());
        }
        return info;
    }

    private void showGPTDescription(View v) {

        String info = getGPTDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getGPTDescription(WordItem word) {
        String info = word.getGPTDescription(helper.language.getPrimaryLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getGPTDescription(helper.language.getSecondaryLanguage());
        }
        return info;
    }

    private void showPrap(View v) {
        String info = getPrap(helper.currentWord);
        showInfo(v, info);
    }

    private String getPrap(WordItem word) {
        String info = word.getPrap(helper.language.getPrimaryLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getPrap(helper.language.getSecondaryLanguage());
        }
        return info;
    }

    private void showInfo(View v, String info) {

        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_design, null, false);


        TextView popupTextView = (TextView) popupView.findViewById(R.id.textPopup); // Идентификатор из popup_layout.xml

        popupTextView.setText(info + "\r\n");

        popupView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                popupWindow.dismiss();
                return true;
            }
        });

        popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.FILL_PARENT, true);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        //popupWindow.showAtLocation(v, Gravity.LEFT, 0, 10);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0); // Отображает всплывающее окно в центре экрана
    }

    MediaPlayerHelper mediaHelper;

    private void playSound() {

        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language = helper.language.getSoundLanguage();;

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

        Queue<String> files = new ArrayDeque<String>();

        List<String> words = AppUtils.getWords(helper.currentWord.getValue(language));

        boolean isValid = false;

        for (String word : words) {
            word = word.replace("|" , "");
            String fileTemplate = AppUtils.getSoundFile(language, word);
            boolean res = AppUtils.addSoundFile(files, fileTemplate);
            if(res && !isArtikle(word)) {
                isValid = true;
            }
        }

        if(!isValid){
            files.clear();
        }
        return files;
    }

    private boolean isArtikle(String value) {
        return "der".equals(value) || "die".equals(value) || "das".equals(value);
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

//                if (Math.abs(x) > 500 && wordsAdapter != null) {
//
//                    int position = wordsAdapter.getPosition(helper.currentWord);
//                    if (x < 0) {
//                        if (position < (wordsAdapter.getCount() - 1)) {
//                            nextWord();
//                        }
//                    } else {
//                        if (position > 0) {
//                            previousWord();
//                        }
//                    }
//                }
                x = 0;
                break;
        }
        return true;
    }
}