package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.CardActivityHelper;
import com.andreaak.cards.adapters.LevelsSpinAdapter;
import com.andreaak.cards.adapters.WordsSpinAdapter;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.configs.Lang;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.FilesHelper;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.cards.utils.xml.XmlParser;
import com.andreaak.common.activitiesShared.FileChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.FilesChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.predicates.StudyFilesPredicate;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import static com.andreaak.cards.utils.FilesHelper.getTempFilePath;
import static com.andreaak.cards.utils.FilesHelper.getWordId;

public class CardActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_FILE = 1;
    public static final String HELPER = "Helper";
    public static final String ALL = "All";
    public static final String A1B2 = "A1-B2";

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

    private Spinner spinnerWords;
    private Spinner spinnerLevels;
    private Spinner spinnerQuantities;

    private CardActivityHelper helper;
    private WordsSpinAdapter spinnerAdapterWords;
    private LevelsSpinAdapter spinnerAdapterLevels;
    private LevelsSpinAdapter spinnerAdapterQuantities;
    private VelocityTracker mVelocityTracker = null;
    private float x;
    private boolean isStudy;
    private boolean isExample;
    private boolean isSelectWord;

    private PopupWindow popupWindow;
    private MediaPlayerHelper mediaHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        findViewById(R.id.buttonStudy).setOnClickListener(this);
        findViewById(R.id.buttonToggle).setOnClickListener(this);
        findViewById(R.id.buttonRemove).setOnClickListener(this);

        buttonSound = findViewById(R.id.buttonSound);
        buttonSound.setOnClickListener(this);

        buttonExample = findViewById(R.id.buttonExample);
        buttonExample.setOnClickListener(this);

        buttonDescription = findViewById(R.id.buttonDescription);
        buttonDescription.setOnClickListener(this);

        buttonGPTDescription = findViewById(R.id.buttonGPTDescription);
        buttonGPTDescription.setOnClickListener(this);

        buttonPrap = findViewById(R.id.buttonPrap);
        buttonPrap.setOnClickListener(this);

        textViewWord1 = findViewById(R.id.textViewWord1);
        textViewTrans1 = findViewById(R.id.textViewTrans1);
        textViewWord2 = findViewById(R.id.textViewWord2);
        textViewTrans2 = findViewById(R.id.textViewTrans2);
        textViewExample = findViewById(R.id.textViewExample);

        onRestoreNonConfigurationInstance();

        forwardTouchesToActivity();
    }

    private void forwardTouchesToActivity() {
        ScrollView scrollView = findViewById(R.id.scrollViewContent);
        scrollView.setOnTouchListener((v, event) -> {
            onTouchEvent(event);
            return false;
        });
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (CardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper != null) {
            helper.isRestore = true;
        } else {
            helper = (CardActivityHelper) getIntent().getSerializableExtra(CardActivity.HELPER);
            if (helper != null && helper.lessonItem != null
                    && helper.lessonItem.wordsCount() == 1
                    && helper.lessonItem.getWords().get(0).getId() == -1) {
                XmlParser.parseLesson(helper.lessonItem);
                String path = getTempFilePath(helper.lessonItem);
                ArrayList<String> list = FilesHelper.getFileLines(path);
                helper.lessonItem.setIgnoredItems(list);
            }
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card, menu);
        menu.setGroupVisible(R.id.groupGoogle, false);

        MenuItem wordsSpinner = menu.findItem(R.id.spinnerWords);
        if (wordsSpinner != null) {
            spinnerWords = (Spinner) wordsSpinner.getActionView();
        }
        MenuItem levelsSpinner = menu.findItem(R.id.spinnerLevels);
        if (levelsSpinner != null) {
            spinnerLevels = (Spinner) levelsSpinner.getActionView();
        }
        MenuItem quantitiesSpinner = menu.findItem(R.id.spinnerQuantities);
        if (quantitiesSpinner != null) {
            spinnerQuantities = (Spinner) quantitiesSpinner.getActionView();
        }

        if (helper != null && helper.lessonItem != null && helper.lessonItem.isContainsWords()) {
            helper.lessonItem.resetLanguage();

            initializeWordsSpinner(
                    helper.lessonItem.getLessonWords(),
                    helper.isSort,
                    helper.lessonItem.getCurrentLanguage()
            );

            if (!helper.isRestore && spinnerAdapterWords != null) {
                helper.currentLevel = spinnerAdapterWords.level;
                helper.currentQuantity = spinnerAdapterWords.quantity;
            }

            if (helper.currentWord == null && spinnerAdapterWords != null) {
                helper.currentWord = spinnerAdapterWords.getItem(0);
            }
        } else if (spinnerWords != null) {
            spinnerWords.setVisibility(View.GONE);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void initializeWordsSpinner(ArrayList<WordItem> words, boolean sort, String language) {
        helper.isSort = sort;
        @SuppressWarnings("unchecked")
        ArrayList<WordItem> copy = (ArrayList<WordItem>) words.clone();
        spinnerAdapterWords = new WordsSpinAdapter(CardActivity.this, android.R.layout.simple_spinner_item, copy, sort, language);
        if (spinnerWords != null) {
            spinnerWords.setAdapter(spinnerAdapterWords);
        }

        List<String> levels = getLevels(words, language);
        spinnerAdapterLevels = new LevelsSpinAdapter(CardActivity.this, android.R.layout.simple_spinner_item, levels);
        if (spinnerLevels != null) {
            spinnerLevels.setAdapter(spinnerAdapterLevels);
        }

        if (helper.currentLevel == null) {
            helper.currentLevel = ALL;
        }
        if (helper.currentQuantity == null) {
            helper.currentQuantity = ALL;
        }

        updateQuantitiesSpinner(helper.currentLevel, helper.currentQuantity);

        if (helper.isRestore) {
            spinnerAdapterWords.setLevel(helper.currentLevel);
            spinnerAdapterWords.setQuantity(helper.currentQuantity);

            int posLevel = spinnerAdapterLevels.getPosition(helper.currentLevel);
            if (spinnerLevels != null && posLevel >= 0) {
                spinnerLevels.setSelected(false);
                spinnerLevels.setSelection(posLevel, true);
            }

            int posWord = spinnerAdapterWords.getPosition(helper.currentWord);
            if (spinnerWords != null && posWord >= 0) {
                spinnerWords.setSelected(false);
                spinnerWords.setSelection(posWord, true);
            }

            activateWord(helper.currentWord, helper.lessonItem.getCurrentLanguage());
            helper.isRestore = false;
        }
        setTitle2(0);

        if (spinnerWords != null) {
            spinnerWords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (!isSelectWord) {
                        WordItem word = spinnerAdapterWords.getItem(position);
                        if (word != null) {
                            helper.currentWord = word;
                            helper.lessonItem.resetLanguage();
                            activateWord(word, helper.lessonItem.getCurrentLanguage());
                        }
                    } else {
                        isSelectWord = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {}
            });
        }

        if (spinnerLevels != null) {
            spinnerLevels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    String selectedLevel = spinnerAdapterLevels.getItem(position);
                    helper.currentLevel = selectedLevel;

                    spinnerAdapterWords.setLevel(selectedLevel);
                    updateQuantitiesSpinner(selectedLevel, helper.currentQuantity);

                    if (spinnerWords != null) {
                        spinnerWords.setSelected(false);
                        spinnerWords.setSelection(0, true);
                    }

                    helper.currentWord = spinnerAdapterWords.getItem(0);
                    if (helper.currentWord != null) {
                        activateWord(helper.currentWord, helper.lessonItem.getCurrentLanguage());
                    }
                    setTitle2(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {}
            });
        }

        if (spinnerQuantities != null) {
            spinnerQuantities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (spinnerAdapterQuantities == null) return;
                    String selectedQuantity = spinnerAdapterQuantities.getItem(position);
                    helper.currentQuantity = selectedQuantity;

                    if (!spinnerAdapterWords.setQuantity(selectedQuantity)) {
                        return;
                    }

                    if (spinnerWords != null) {
                        spinnerWords.setSelected(false);
                        spinnerWords.setSelection(0, true);
                    }

                    helper.currentWord = spinnerAdapterWords.getItem(0);
                    if (helper.currentWord != null) {
                        activateWord(helper.currentWord, helper.lessonItem.getCurrentLanguage());
                    }
                    setTitle2(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {}
            });
        }
    }

    private void updateQuantitiesSpinner(String currentLevel, String preferredQuantity) {
        if (helper == null || helper.lessonItem == null || spinnerQuantities == null) {
            return;
        }
        String language = helper.lessonItem.getCurrentLanguage();
        List<WordItem> wordsForLevel = WordsSpinAdapter.getWordsForLevel(
                helper.lessonItem.getLessonWords(),
                language,
                currentLevel
        );
        List<String> quantities = getQuantities(wordsForLevel, language);

        spinnerAdapterQuantities = new LevelsSpinAdapter(CardActivity.this, android.R.layout.simple_spinner_item, quantities);
        spinnerQuantities.setAdapter(spinnerAdapterQuantities);

        String targetQuantity = preferredQuantity != null && quantities.contains(preferredQuantity) ? preferredQuantity : ALL;
        int pos = spinnerAdapterQuantities.getPosition(targetQuantity);
        if (pos < 0) {
            pos = 0;
        }
        spinnerQuantities.setSelected(false);
        spinnerQuantities.setSelection(pos, true);
        spinnerAdapterWords.setQuantity(targetQuantity);
        helper.currentQuantity = targetQuantity;
    }

    private List<String> getQuantities(List<WordItem> words, String language) {
        Set<String> set = new TreeSet<>((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });

        for (WordItem w : words) {
            String q = WordsSpinAdapter.normalizeQuantity(w.getQuantity(language));
            set.add(q);
        }

        List<String> list = new ArrayList<>(set);
        list.add(0, ALL);
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_sort) {
            sortSpinner();
            return true;
        } else if (id == R.id.menu_restore) {
            clearWords();
            return true;
        } else if (id == R.id.menu_createFile) {
            createFile();
            return true;
        } else if (id == R.id.menu_addItemToFile) {
            addItemToFile();
            return true;
        } else if (id == R.id.menu_openFile) {
            openFile();
            return true;
        } else if (id == R.id.menu_shuffle_words) {
            shuffleWords();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK && data != null) {
            String name = (String) data.getSerializableExtra(FileChooserWithButtonsActivity.FILE_NAME);
            openFile(name);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sortSpinner() {
        helper.isSort = !helper.isSort;
        List<WordItem> filtered = spinnerAdapterWords.setSort(helper.isSort);

        spinnerAdapterWords.clear();
        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);
        spinnerWords.setSelection(position, true);
    }

    private void shuffleWords() {
        List<WordItem> filtered = spinnerAdapterWords.shuffle();

        spinnerAdapterWords.clear();
        spinnerAdapterWords.addAll(filtered);
        spinnerAdapterWords.notifyDataSetChanged();

        int position = spinnerAdapterWords.getPosition(helper.currentWord);
        spinnerWords.setSelected(false);
        spinnerWords.setSelection(position, true);
    }

    private List<String> getLevels(ArrayList<WordItem> words, final String language) {
        Set<String> s = new TreeSet<>();

        for (WordItem w : words) {
            if (!Utils.isEmpty(w.getLevel(language))) {
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

        if (id == R.id.buttonToggle) {
            toggle();
        } else if (id == R.id.buttonSound) {
            playSound();
        } else if (id == R.id.buttonStudy) {
            showStudy();
        } else if (id == R.id.buttonExample) {
            showExample();
        } else if (id == R.id.buttonDescription) {
            showDescription(v);
        } else if (id == R.id.buttonGPTDescription) {
            showGPTDescription(v);
        } else if (id == R.id.buttonPrap) {
            showPrap(v);
        } else if (id == R.id.buttonRemove) {
            removeWord();
        }
    }

    private void activateWord(WordItem word, String lesson) {
        if (isStudy) {
            activateStudyWord(word);
        } else {
            activateCheckWord(word, lesson);
        }
    }

    private void activateCheckWord(WordItem word, String lang) {
        setVisibility(textViewWord2, View.GONE);
        setVisibility(textViewTrans2, View.GONE);
        setVisibility(textViewExample, View.GONE);
        setTranscriptionAndVisibility(textViewExample, "");
        buttonExample.setVisibility(View.INVISIBLE);
        buttonDescription.setVisibility(View.INVISIBLE);
        buttonGPTDescription.setVisibility(View.INVISIBLE);
        buttonPrap.setVisibility(View.INVISIBLE);

        String wordText = word.getValue(lang);
        setWordAndVisibility(textViewWord1, wordText, R.color.colorBlack);
        AppUtils.adoptFontSize(textViewWord1, wordText, 45, 35);

        String transcription = word.getTranscription(lang);
        String info = word.getInfo(lang);
        String level = word.getLevel(lang);
        String quantity = word.getQuantity(lang);
        String text = combineText(combineText(combineText(transcription, info), level), quantity);
        setTranscriptionAndVisibility(textViewTrans1, text);

        Queue<String> files = getSoundFiles(lang);
        boolean isVisible = !files.isEmpty();
        int flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void activateStudyWord(WordItem word) {
        LanguageItem languageItem = helper.lessonItem.getLanguageItem();

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        setWordAndVisibility(textViewWord1, wordText, R.color.colorBlack);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String level = word.getLevel(languageItem.getPrimaryLanguage());
        String quantity = word.getQuantity(languageItem.getPrimaryLanguage());
        String text = combineText(combineText(combineText(transcription, info), level), quantity);
        setTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        setWordAndVisibility(textViewWord2, wordText, R.color.colorBlue);
        AppUtils.adoptFontSize(textViewWord2, wordText, 25, 15);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        quantity = word.getQuantity(languageItem.getSecondaryLanguage());
        text = combineText(combineText(transcription, info), quantity);
        setTranscriptionAndVisibility(textViewTrans2, text);

        String example = word.getExample(helper.lessonItem.getCurrentLanguage());
        if (Utils.isEmpty(example)) {
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

        if (isExample && !Utils.isEmpty(example)) {
            setTranscriptionAndVisibility(textViewExample, example);
            AppUtils.adoptFontSize(textViewExample, example, 25, 15);
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

    private void setWordAndVisibility(TextView textView, String word, int color) {
        if (!Utils.isEmpty(word)) {
            textView.setText(word);
            String lowerWord = word.toLowerCase();
            if (lowerWord.startsWith("der ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
            } else if (lowerWord.startsWith("die ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            } else if (lowerWord.startsWith("das ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
            } else {
                textView.setTextColor(ContextCompat.getColor(this, color));
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

    private void setVisibility(TextView textView, int flag) {
        if (textView.getVisibility() != flag) {
            textView.setVisibility(flag);
        }
    }

    private void toggle() {
        String language = helper.lessonItem.getOtherLanguage();
        activateWord(helper.currentWord, language);
    }

    private void showStudy() {
        isStudy = !isStudy;
        activateWord(helper.currentWord, helper.lessonItem.getCurrentLanguage());
    }

    private void showExample() {
        isExample = !isExample;
        activateWord(helper.currentWord, helper.lessonItem.getCurrentLanguage());
    }

    private interface WordFieldGetter {
        String get(WordItem item, String lang);
    }

    private String getLocalizedWordField(WordItem word, WordFieldGetter getter) {
        if (word == null || helper == null || helper.lessonItem == null) {
            return "";
        }
        String info = getter.get(word, helper.lessonItem.getCurrentLanguage());
        if (Utils.isEmpty(info)) {
            info = getter.get(word, helper.lessonItem.getOtherLanguage());
        }
        return info != null ? info : "";
    }

    private void showDescription(View v) {
        String info = getDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getDescription(WordItem word) {
        return getLocalizedWordField(word, WordItem::getDescription);
    }

    private void showGPTDescription(View v) {
        String info = getGPTDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getGPTDescription(WordItem word) {
        String gpt = getLocalizedWordField(word, WordItem::getGPTDescription);
        String wb = getWBDescription(word);
        String fileName = (helper != null && helper.lessonItem != null) ? helper.lessonItem.getFileName() : "";

        StringBuilder sb = new StringBuilder();
        if (!Utils.isEmpty(gpt)) {
            sb.append(gpt);
        }
        if (!Utils.isEmpty(wb)) {
            if (sb.length() > 0) sb.append("\r\n");
            sb.append(wb);
        }
        if (!Utils.isEmpty(fileName)) {
            if (sb.length() > 0) sb.append("\r\n");
            sb.append(fileName);
        }
        return sb.toString();
    }

    private String getWBDescription(WordItem word) {
        return getLocalizedWordField(word, WordItem::getWBDescription);
    }

    private void showPrap(View v) {
        String info = getPrap(helper.currentWord);
        showInfo(v, info);
    }

    private String getPrap(WordItem word) {
        return getLocalizedWordField(word, WordItem::getPrap);
    }

    private void showInfo(View v, String info) {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_design, findViewById(android.R.id.content), false);

        TextView popupTextView = popupView.findViewById(R.id.textPopup);
        popupTextView.setText(String.format("%s\r\n", info));

        popupView.setOnTouchListener((view, event) -> {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            view.performClick();
            return true;
        });

        popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
    }

    private void removeWord() {
        String path = getTempFilePath(helper.lessonItem);
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
        if (helper == null || helper.lessonItem == null || helper.lessonItem.getLanguageItem() == null) {
            return;
        }
        String lang1 = helper.lessonItem.getLanguageItem().getPrimaryLanguage();
        String lang2 = helper.lessonItem.getLanguageItem().getSecondaryLanguage();

        if (Lang.isRu(lang1)) {
            XmlParser.addItemToFile(helper.currentWord, lang1, lang2);
        } else {
            XmlParser.addItemToFile(helper.currentWord, lang2, lang1);
        }
    }

    private void openFile() {
        Intent intent = new Intent(this, FileChooserWithButtonsActivity.class);
        intent.putExtra(FilesChooserWithButtonsActivity.PREDICATE, new StudyFilesPredicate());
        intent.putExtra(FilesChooserWithButtonsActivity.TITLE, getString(R.string.combine_files));
        intent.putExtra(FilesChooserWithButtonsActivity.INITIAL_PATH, AppConfigs.getInstance().getStudyDir());
        startActivityForResult(intent, REQUEST_FILE);
    }

    private void openFile(String name) {
        XmlParser.openFile(name);
    }

    private void playSound() {
        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language;
        if (isStudy) {
            LanguageItem languageItem = helper.lessonItem.getLanguageItem();
            language = languageItem.getSoundLanguage();
        } else {
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
        Queue<String> files = new ArrayDeque<>();
        List<String> words = AppUtils.getWords(helper.currentWord.getValue(language));
        boolean isValid = false;

        for (String word : words) {
            word = word.replace("|", "");
            String fileTemplate = AppUtils.getSoundFile(language, word);
            boolean res = AppUtils.addSoundFile(files, fileTemplate);
            if (res && !isArticle(word)) {
                isValid = true;
            }
        }

        if (!isValid) {
            files.clear();
        }
        return files;
    }

    private boolean isArticle(String value) {
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

        setTitle2(position + index);

        position = spinnerAdapterWords.getPosition(helper.currentWord);
        isSelectWord = true;
        spinnerWords.setSelected(false);
        spinnerWords.setSelection(position, false);
        activateWord(word, helper.lessonItem.getCurrentLanguage());
    }

    private void setTitle2(int index) {
        String fileName = Cache.getInstance().getItem(XmlParser.CurrentStudyFileKey);

        setTitle((index + 1) + " of " + spinnerAdapterWords.getCount()
                + (Utils.isEmpty(fileName) ? "" : (" " + fileName)));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    x = mVelocityTracker.getXVelocity(pointerId);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                x = 0;
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (mediaHelper != null) {
            mediaHelper.stop();
        }
        super.onDestroy();
    }
}
