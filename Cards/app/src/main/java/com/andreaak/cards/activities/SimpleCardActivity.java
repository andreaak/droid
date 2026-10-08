package com.andreaak.cards.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TabStopSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.SimpleCardActivityHelper;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.MediaPlayerHelper;
import com.andreaak.cards.utils.xml.XmlParser;
import com.andreaak.common.activitiesShared.FileChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.FilesChooserWithButtonsActivity;
import com.andreaak.common.activitiesShared.HandleExceptionAppCompatActivity;
import com.andreaak.common.predicates.StudyFilesPredicate;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class SimpleCardActivity extends HandleExceptionAppCompatActivity implements View.OnClickListener {

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

    private boolean isExample;
    private boolean isDescription;

    private PopupWindow popupWindow;
    private MediaPlayerHelper mediaHelper;

    private final ActivityResultLauncher<Intent> openFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String name = (String) result.getData().getSerializableExtra(FileChooserWithButtonsActivity.FILE_NAME);
                    openFile(name);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        findViewById(R.id.buttonStudy).setVisibility(View.INVISIBLE);
        findViewById(R.id.buttonToggle).setVisibility(View.INVISIBLE);
        findViewById(R.id.buttonRemove).setVisibility(View.INVISIBLE);

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
    }

    @SuppressWarnings("deprecation")
    private void onRestoreNonConfigurationInstance() {
        helper = (SimpleCardActivityHelper) getLastCustomNonConfigurationInstance();
        if (helper == null) {
            helper = (SimpleCardActivityHelper) getIntent().getSerializableExtra(SimpleCardActivity.HELPER);
        }
        if (helper == null) {
            return;
        }
        setTitle(helper.currentSimpleWord.getDisplayName(helper.language.getPrimaryLanguage()));
        String lang = helper.language.getPrimaryLanguage();
        WordItem res = XmlParser.getWordItem(helper.currentSimpleWord, lang);

        if (res != null) {
            helper.currentWord = res;
            activateWord(res);
            setTitle2();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_simple, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainCustomNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_plus || id == R.id.menu_minus) {
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.buttonSound) {
            playSound();
        } else if (id == R.id.buttonExample) {
            showExample();
        } else if (id == R.id.buttonDescription) {
            showDescription(v);
        } else if (id == R.id.buttonGPTDescription) {
            showGPTDescription(v);
        } else if (id == R.id.buttonPrap) {
            showPrap(v);
        }
    }

    private void activateWord(WordItem word) {
        if (word == null || helper == null || helper.language == null) {
            return;
        }

        LanguageItem languageItem = helper.language;

        String wordText = word.getValue(languageItem.getPrimaryLanguage());
        setWordAndVisibility(textViewWord1, wordText);

        String transcription = word.getTranscription(languageItem.getPrimaryLanguage());
        String info = word.getInfo(languageItem.getPrimaryLanguage());
        String level = word.getLevel(languageItem.getPrimaryLanguage());
        String quantity = word.getQuantity(languageItem.getPrimaryLanguage());
        String text = combineText(combineText(combineText(transcription, info), level), quantity);
        setTranscriptionAndVisibility(textViewTrans1, text);

        wordText = word.getValue(languageItem.getSecondaryLanguage());
        setWordAndVisibility(textViewWord2, wordText);
        adoptFontSize(textViewWord2, wordText, 45, 25);

        transcription = word.getTranscription(languageItem.getSecondaryLanguage());
        info = word.getInfo(languageItem.getSecondaryLanguage());
        quantity = word.getQuantity(languageItem.getSecondaryLanguage());
        text = combineText(combineText(transcription, info), quantity);
        setTranscriptionAndVisibility(textViewTrans2, text);

        String example = word.getExample(languageItem.getPrimaryLanguage());
        if (Utils.isEmpty(example)) {
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

        if (isExample && !Utils.isEmpty(example)) {
            textViewExample.setGravity(Gravity.CENTER);
            setTranscriptionAndVisibility(textViewExample, example);
        } else if (isDescription && !Utils.isEmpty(description)) {
            textViewExample.setGravity(Gravity.START);
            setTranscriptionAndVisibility(textViewExample, description);
        } else {
            setVisibility(textViewExample, View.GONE);
        }

        Queue<String> files = getSoundFiles(languageItem.getSoundLanguage());
        boolean isVisible = !files.isEmpty();
        flag = isVisible ? View.VISIBLE : View.INVISIBLE;
        buttonSound.setVisibility(flag);
    }

    private void adoptFontSize(TextView textView, String text, float minFontSize, float defaultFontSize) {
        textView.post(() -> {
            if (Utils.isEmpty(text)) {
                return;
            }

            String[] items = text.split("\n");
            String textLine = "";
            for (String item : items) {
                if (textLine.length() < item.length()) {
                    textLine = item;
                }
            }

            int displayWidth = textView.getMeasuredWidth();
            if (displayWidth <= 0) {
                return;
            }

            float density = textView.getResources().getDisplayMetrics().density;
            float defaultFontSize2 = defaultFontSize * density;
            Paint paint = new Paint();
            paint.setTextSize(defaultFontSize2);
            float widthPx = paint.measureText(textLine);
            if (widthPx > displayWidth) {
                float fontSize = Math.max(minFontSize, displayWidth / widthPx * defaultFontSize2);
                setViewTextSize(textView, fontSize);
                setViewGravity(textView, false);
            } else {
                setViewTextSize(textView, defaultFontSize2);
                setViewGravity(textView, true);
            }
        });
    }

    private void setViewGravity(TextView textView, boolean isCenter) {
        if (isCenter) {
            textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else {
            textView.setGravity(Gravity.START);
        }
        textView.setPadding(0, 0, 0, 0);
    }

    private void setViewTextSize(TextView textView, float size) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        textView.setPadding(0, 0, 0, 0);
    }

    private String combineText(String first, String second) {
        return first == null ? second : second == null ? first : first + "  " + second;
    }

    private void setWordAndVisibility(TextView textView, String word) {
        if (!Utils.isEmpty(word)) {
            textView.setText(word);
            String lowerWord = word.toLowerCase();
            if (lowerWord.startsWith("der ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorBlue));
            } else if (lowerWord.startsWith("die ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            } else if (lowerWord.startsWith("das ")) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
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

    private void showExample() {
        isExample = !isExample;
        isDescription = false;
        activateWord(helper.currentWord);
    }

    private interface WordFieldGetter {
        String get(WordItem item, String lang);
    }

    private String getLocalizedField(WordItem word, WordFieldGetter getter) {
        if (word == null || helper == null || helper.language == null) {
            return "";
        }
        String info = getter.get(word, helper.language.getPrimaryLanguage());
        if (Utils.isEmpty(info)) {
            info = getter.get(word, helper.language.getSecondaryLanguage());
        }
        return info != null ? info : "";
    }

    private void showDescription(View v) {
        String info = getDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getDescription(WordItem word) {
        return getLocalizedField(word, WordItem::getDescription);
    }

    private void showGPTDescription(View v) {
        String info = getGPTDescription(helper.currentWord);
        showInfo(v, info);
    }

    private String getGPTDescription(WordItem word) {
        String gpt = getLocalizedField(word, WordItem::getGPTDescription);
        String wb = getWBDescription(word);
        String path = (word != null && word.getPath() != null) ? word.getPath() : "";

        StringBuilder sb = new StringBuilder();
        if (!Utils.isEmpty(gpt)) {
            sb.append(gpt);
        }
        if (!Utils.isEmpty(wb)) {
            if (sb.length() > 0) sb.append("\r\n\r\n");
            sb.append(wb);
        }
        if (!Utils.isEmpty(path)) {
            if (sb.length() > 0) sb.append("\r\n\r\n");
            sb.append(path);
        }
        return sb.toString();
    }

    private String getWBDescription(WordItem word) {
        return getLocalizedField(word, WordItem::getWBDescription);
    }

    private void showPrap(View v) {
        String info = getPrap(helper.currentWord);
        showInfo(v, info);
    }

    private String getPrap(WordItem word) {
        return getLocalizedField(word, WordItem::getPrap);
    }

    private void createFile() {
        XmlParser.createFile();
        setTitle2();
    }

    private void addItemToFile() {
        String lang1 = helper.language.getPrimaryLanguage();
        String lang2 = helper.language.getSecondaryLanguage();

        if ("ru".equals(lang1)) {
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
        openFileLauncher.launch(intent);
    }

    private void openFile(String name) {
        XmlParser.openFile(name);
        setTitle2();
    }

    private void setTitle2() {
        String fileName = Cache.getInstance().getItem(XmlParser.CurrentStudyFileKey);
        setTitle(Utils.isEmpty(fileName) ? "" : (" " + fileName));
    }

    private void showInfo(View v, String info) {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_design, findViewById(android.R.id.content), false);

        TextView popupTextView = popupView.findViewById(R.id.textPopup);

        SpannableString spannableString = new SpannableString(info + "\r\n");

        int tabSpacePixels = 50;

        spannableString.setSpan(
                new TabStopSpan.Standard(tabSpacePixels),
                0,
                info.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        popupTextView.setText(spannableString);

        popupView.setOnTouchListener((view, motionEvent) -> {
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

    private void playSound() {
        if (mediaHelper != null && mediaHelper.IsActive) {
            return;
        }

        String language = helper.language.getSoundLanguage();

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
