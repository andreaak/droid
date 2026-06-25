package com.andreaak.cards.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.VelocityTracker;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.adapters.LevelsSpinAdapter;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.FilesHelper;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import static com.andreaak.cards.utils.AppUtils.setViewGravity;
import static com.andreaak.cards.utils.FilesHelper.getTempFilePath;
import static com.andreaak.cards.utils.FilesHelper.getWordId;

public class CardActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    //in
    public static final String HELPER = "Helper";
    public static final String ALL = "All";
    public static final String A1B2 = "A1-B2";

    private ImageButton buttonStudy;
//    private ImageButton buttonToggle;
    private ImageButton buttonSound;
    private ImageButton buttonExample;
    private ImageButton buttonDescription;
    private ImageButton buttonGPTDescription;
    private ImageButton buttonPrap;
//    private ImageButton buttonRemove;

    private TextView textViewWord1;
    private TextView textViewTrans1;
    private TextView textViewWord2;
    private TextView textViewTrans2;
    private TextView textViewExample;
    private LinearLayout texts;

    private Spinner spinnerWords;
    private Spinner spinnerLevels;

    private Menu menu;

    private CardActivityHelper helper;
    //private WordsSpinAdapter wordsAdapter;
    private WordsSpinAdapter spinnerAdapterWords;
    private LevelsSpinAdapter spinnerAdapterLevels;
    private VelocityTracker mVelocityTracker = null;
    private float x;
    private boolean isStudy;
    private boolean isExample;
    private boolean isSelectWord;

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

        (findViewById(R.id.buttonStudy)).setOnClickListener(this);
        (findViewById(R.id.buttonToggle)).setOnClickListener(this);
        (findViewById(R.id.buttonRemove)).setOnClickListener(this);

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
        texts = (LinearLayout) findViewById(R.id.texts);

        //setFontSize();
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (CardActivityHelper) getIntent()
                    .getSerializableExtra(CardActivity.HELPER);
            if(helper.lessonItem.wordsCount() == 1 && helper.lessonItem.getWords().get(0).getId() == -1) {
                XmlParser.parseLesson(helper.lessonItem);
                String path = getTempFilePath(helper.lessonItem);
                ArrayList<String> list = FilesHelper.getFileLines(path);
                helper.lessonItem.setIgnoredItems(list);
            }
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
        menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, false /*googleDriveHelper.isConnected()*/);

        MenuItem wordsSpinner = menu.findItem(R.id.spinnerWords);
        spinnerWords = (android.widget.Spinner) wordsSpinner.getActionView();
        MenuItem levelsSpinner = menu.findItem(R.id.spinnerLevels);
        spinnerLevels = (android.widget.Spinner) levelsSpinner.getActionView();

        if (helper.lessonItem.isContainsWords()) {

            helper.lessonItem.resetLanguage();

            initializeWordsSpinner(helper.lessonItem.getLessonWords(), helper.isSort/*helper.lessonItem.isSortItems()*/,
                    helper.lessonItem.getCurrentLanguage());

            if(!helper.isRestore) {
                helper.currentLevel = spinnerAdapterWords.level;
            }

            if(helper.currentWord == null) {
                helper.currentWord = spinnerAdapterWords.getItem(0);
            }
        } else {
            spinnerWords.setVisibility(View.GONE);
        }

        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void initializeWordsSpinner(ArrayList<WordItem> words, boolean sort, String language) {

        helper.isSort = sort;
        ArrayList<WordItem> copy = (ArrayList<WordItem>)words.clone();
        spinnerAdapterWords = new WordsSpinAdapter(CardActivity.this,
                android.R.layout.simple_spinner_item,
                copy, sort, language);

        spinnerWords.setAdapter(spinnerAdapterWords);

        setTitle(copy.size() + " " + helper.lessonItem.getDisplayName());
        List<String> levels = GetLevels(words, language);

        spinnerAdapterLevels = new LevelsSpinAdapter(CardActivity.this,
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

            helper.isRestore = false;
        }

        spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {

                if(!isSelectWord) {
                    WordItem word = spinnerAdapterWords.getItem(position);
                    helper.currentWord = word;
                    helper.lessonItem.resetLanguage();

                    activateWord(word);
                } else {
                    isSelectWord = false;
                }
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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.menu_plus)  {
            textBigger();
            return true;
        } else if(id == R.id.menu_minus)  {
            textSmaller();
            return true;
        } else if(id == R.id.menu_sort)  {
            sortSpinner();
            return true;
        } else if(id == R.id.menu_restore)  {
            clearWords();
            return true;
        }else if(id == R.id.menu_createFile)  {
            createFile();
            return true;
        }else if(id == R.id.menu_addItemToFile)  {
            addItemToFile();
            return true;
        }else if(id == R.id.menu_openLastFile)  {
            openLastFile();
            return true;
        } else if(id == R.id.menu_shuffle_words)  {
            shuffleWords();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortSpinner() {
        helper.isSort = !helper.isSort;
        List<WordItem> filtered = spinnerAdapterWords.setSort(helper.isSort);

        spinnerAdapterWords.clear();

        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, true);
    }

    private void shuffleWords() {
        List<WordItem> filtered = spinnerAdapterWords.shuffle();

        spinnerAdapterWords.clear();

        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, true);
    }

//    private void setFontSize() {
//        float factor = SharedPreferencesHelper.getInstance().getFloat(AppConfigs.SP_TEXT_FONT_SIZE);
//        setTextSize(factor);
//    }



    private void saveFontSize(float factor) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_TEXT_FONT_SIZE, factor);
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
        setTextSize(textViewWord1, factor, true);
        setTextSize(textViewTrans1, factor, true);
        setTextSize(textViewWord2, factor, false);
        setTextSize(textViewTrans2, factor, true);
        setTextSize(textViewExample, factor, true);
        saveFontSize(factor);
    }

    private void setTextSize(TextView view, float factor, boolean isCenter) {
        float newSize = view.getTextSize() * factor;
        AppUtils.setViewTextSize(view, newSize);
        AppUtils.setViewGravity(view, isCenter);
    }



    private List<String> GetLevels(ArrayList<WordItem> words, final String language) {

        Set<String> s = new TreeSet<>();

        for(WordItem w : words) {
            if(!Utils.isEmpty(w.getLevel(language))) {
                s.add(w.getLevel(language));
            } else {
                s.add("CC");
            }
        }

        List<String> list = new ArrayList<>(s);
        list.add(0, ALL);
        list.add(A1B2);
        return list;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonToggle)  {
            toggle();
        } else if(id == R.id.buttonSound)  {
            playSound();
        } else if(id == R.id.buttonStudy)  {
            showStudy();
        } else if(id == R.id.buttonExample)  {
            showExample();
        } else if(id == R.id.buttonDescription)  {
            showDescription(v);
        } else if(id == R.id.buttonGPTDescription)  {
            showGPTDescription(v);
        } else if(id == R.id.buttonPrap)  {
            showPrap(v);
        } else if(id == R.id.buttonRemove)  {
            removeWord(v);
        }
    }

    private void activateWord(WordItem word) {
        if(isStudy) {
            activateStudyWord(word);
        } else {
            activateCheckWord(word);
        }
    }

    private void activateCheckWord(WordItem word) {

        setVisibility(textViewWord2, View.GONE);
        setVisibility(textViewTrans2, View.GONE);
        setVisibility(textViewExample, View.GONE);
        setTranscriptionAndVisibility(textViewExample, "");
        buttonExample.setVisibility(View.INVISIBLE);
        buttonDescription.setVisibility(View.INVISIBLE);
        buttonGPTDescription.setVisibility(View.INVISIBLE);
        buttonPrap.setVisibility(View.INVISIBLE);

        String wordText = word.getValue(helper.lessonItem.getCurrentLanguage());
        SetWordAndVisibility(textViewWord1, wordText);
        AppUtils.adoptFontSize(textViewWord1, wordText, 45, 38);

        String transcription = word.getTranscription(helper.lessonItem.getCurrentLanguage());
        String info = word.getInfo(helper.lessonItem.getCurrentLanguage());
        String level = word.getLevel(helper.lessonItem.getCurrentLanguage());
        String text = combineText(combineText(transcription, info), level);
        setTranscriptionAndVisibility(textViewTrans1, text);

        Queue<String> files = getSoundFiles(helper.lessonItem.getCurrentLanguage());
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void activateStudyWord(WordItem word) {

        LanguageItem languageItem = helper.lessonItem.getLanguageItem();

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        SetWordAndVisibility(textViewWord1, wordText);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String level = word.getLevel(languageItem.getPrimaryLanguage());
        String text = combineText(combineText(transcription, info), level);
        setTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        SetWordAndVisibility(textViewWord2, wordText);
        AppUtils.adoptFontSize(textViewWord2, wordText, 45, 25);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        text = combineText(transcription, info);
        setTranscriptionAndVisibility(textViewTrans2, text);

        String example = word.getExample(helper.lessonItem.getCurrentLanguage());
        if(Utils.isEmpty(example)) {
            example = word.getExample(helper.lessonItem.getOtherLanguage());
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
            setTranscriptionAndVisibility(textViewExample, example);
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

    private void setTranscriptionAndVisibility(TextView textView, String transcription) {
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

    private void toggle() {
        helper.lessonItem.ToggleLanguage();
        activateWord(helper.currentWord);
    }

    private void showStudy() {
        isStudy = !isStudy;
        activateWord(helper.currentWord);
    }

    private void showExample() {
        isExample = !isExample;
        activateWord(helper.currentWord);
    }

    PopupWindow popupWindow;
    View popupView;

    private void showDescription(View v) {

        String info = getDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getDescription(WordItem word) {
        String info = word.getDescription(helper.lessonItem.getCurrentLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getDescription(helper.lessonItem.getOtherLanguage());
        }
        return info;
    }

    private void showGPTDescription(View v) {

        String info = getGPTDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getGPTDescription(WordItem word) {
        String info = word.getGPTDescription(helper.lessonItem.getCurrentLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getGPTDescription(helper.lessonItem.getOtherLanguage());
        }
        return info;
    }

    private void showPrap(View v) {
        String info = getPrap(helper.currentWord);
        showInfo(v, info);
    }

    private String getPrap(WordItem word) {
        String info = word.getPrap(helper.lessonItem.getCurrentLanguage());
        if(Utils.isEmpty(info)) {
            info = word.getPrap(helper.lessonItem.getOtherLanguage());
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

    private void removeWord(View v) {
        String path =  getTempFilePath(helper.lessonItem);
        String wordId = getWordId(helper.currentWord, helper.lessonItem.getCurrentLanguage());
        FilesHelper.addLineToFile(path, wordId);
    }

    private void clearWords() {
        String path = getTempFilePath(helper.lessonItem);
        FilesHelper.deleteFile(path);
    }

    private void createFile() {
        XmlParser.createFile();
    }

    private void addItemToFile() {

        String lang1 = helper.lessonItem.getLanguageItem().getPrimaryLanguage();
        String lang2 = helper.lessonItem.getLanguageItem().getSecondaryLanguage();

        if("ru".equals(lang1)) {
            XmlParser.addItemToFile(helper.currentWord, lang1, lang2);
        } else {
            XmlParser.addItemToFile(helper.currentWord, lang2, lang1);
        }
    }

    private void openLastFile() {
        XmlParser.openLastFile();
    }

    MediaPlayerHelper mediaHelper;

    private void playSound() {

        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language;
        if(isStudy){
            LanguageItem languageItem = helper.lessonItem.getLanguageItem();
            language = languageItem.getSoundLanguage();
        }else {
            language = helper.lessonItem.getCurrentLanguage();
        }

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

    private void previousWord() {
        setWord(-1);
    }

    private void nextWord() {
        setWord(1);
    }

    private void setWord(int index) {
        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        WordItem word = spinnerAdapterWords.getItem(position + index);
        helper.currentWord = word;
        setTitle(helper.lessonItem.getDisplayName() + " " + (position + index + 1) +
                " of " + spinnerAdapterWords.getCount()
        + " " + Cache.getInstance().getItem(XmlParser.FileKey));
        position = spinnerAdapterWords.getPosition(helper.currentWord);
        isSelectWord = true;
        spinnerWords.setSelected(false);// must
        spinnerWords.setSelection(position, false);
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
}