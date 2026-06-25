package com.andreaak.cards.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andreaak.cards.R;
import com.andreaak.cards.adapters.AITextViewAdapter;
import com.andreaak.cards.adapters.LessonsSpinAdapter;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.utils.Cache;
import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAiActivity extends HandleExceptionActivity implements View.OnClickListener {

    private final String modelName = "gpt-4.1-mini";
    private final String apiKey = "";//alexandrzpua19

    private AutoCompleteTextView editPrompt;
    private AITextViewAdapter lessonsAdapter;


    private Button buttonAsk;

    private Button buttonAskRu;

    private Button buttonSave;

    private TextView textResult;

    private String aiFolder;

    private OkHttpClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView( R.layout.activity_ai );

        editPrompt = findViewById(R.id.editPrompt);



        textResult = findViewById(R.id.textResult);

        buttonAsk = findViewById(R.id.buttonAsk);
        buttonAsk.setOnClickListener(this);

        buttonAskRu = findViewById(R.id.buttonAskRu);
        buttonAskRu.setOnClickListener(this);

        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(this);

        aiFolder = AppConfigs.getInstance().getAIDir();

        setTitle("AI");

        ArrayList<String> values = loadWordsFile(AppConfigs.getInstance().WorkingDir, "index.dic");

        lessonsAdapter = new AITextViewAdapter(OpenAiActivity.this,
                R.layout.item_autocomplete,
                values);
        editPrompt.setAdapter(lessonsAdapter);

        editPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                editPrompt.showDropDown();
            }
        });

        editPrompt.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //selectedWord = lessonsAdapter.getItem(position);
            }
        });

        client =
                new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(180, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .callTimeout(180, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonAsk)  {
            ask();
        } else if(id == R.id.buttonAskRu)  {
            askRu();
        }  else if(id == R.id.buttonSave)  {
            save();
        }
    }

    private void ask() {
        String word =  editPrompt.getText().toString().trim();
        if(!Utils.isEmpty(word)) {
            String data = loadResponseFromFile(aiFolder,  word);
            if(!Utils.isEmpty(data)) {
                textResult.setText(data);
            } else {
                String lang = "de";
                askOpenAi(getAIPrompt(lang, word));
            }
        }
    }

    private void askRu() {
        String word =  editPrompt.getText().toString().trim();
        if(!Utils.isEmpty(word)) {
            String data = loadResponseFromFile(aiFolder,  word);
            if(!Utils.isEmpty(data)) {
                textResult.setText(data);
            } else {
                String lang = "de";
                askOpenAi(getAIRuPrompt(lang, word));
            }
        }
    }

    private void save() {
        String word =  editPrompt.getText().toString();
        String text =  textResult.getText().toString();
        if(!Utils.isEmpty(word) && !Utils.isEmpty(text)) {
            saveResponseToFile(aiFolder, word, text);
        }
    }

    private String getAIPrompt(String lang, String word) {

        String langItem;
        switch (lang)
        {
            case "de":
                langItem = "немецкого";
                break;
            case "en":
                langItem = "английского";
                break;
            default:
                return null;
        }

        return String.format("значение и типы %1$s слова %2$s " +
                "с переводом на русский, примерами, синонимами, антонимами, предложное управление, " +
                "транскрипция и также уровень слова", langItem, word);
    }

    private String getAIRuPrompt(String lang, String word) {

        String langItem;
        switch (lang)
        {
            case "de":
                langItem = "немецкий";
                break;
            case "en":
                langItem = "английский";
                break;
            default:
                return null;
        }

        return String.format("переведи слово %1$s на %2$s, " +
                "добавь также следующую информацию об иностранном слове: примеры, синонимы, антонимы, предложное управление, " +
                "транскрипция и также уровень слова", word, langItem);
    }

    private void askOpenAi(String prompt) {

        buttonAsk.setEnabled(false);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String response = requestOpenAi(prompt);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            textResult.setText(response);
                                            buttonAsk.setEnabled(true);
                                        }
                                    }
                            );

                        } catch (Exception ex) {

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            textResult.setText(ex.getMessage());
                                            buttonAsk .setEnabled(true);
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }

    private String requestOpenAi(
            String prompt)
            throws Exception {

        JSONObject root = new JSONObject();

        root.put( "model", modelName);

        JSONArray messages = new JSONArray();

        JSONObject user = new JSONObject();

        user.put("role", "user");
        user.put("content", prompt);
        messages.put(user);
        root.put("messages", messages);

        RequestBody body = RequestBody.create( root.toString(),
                        MediaType.parse(
                                "application/json"
                        )
                );

        Request request = new Request.Builder()
                        .url(
                                "https://api.openai.com/v1/chat/completions"
                        )
                        .addHeader(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .post(body)

                        .build();

        Response response = client.newCall(request).execute();

        String json = response.body() .string();

        JSONObject result = new JSONObject(json);

        return result
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    private void saveResponseToFile(String folder, String word, String text) {

        try {
            File dir = new File(folder);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, getFileName(word));

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(text.getBytes("UTF-8"));
            }
        } catch (Exception ex) {
            Toast.makeText(
                    OpenAiActivity.this,
                    ex.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            setTitle("Error");
        }
   }

    private String loadResponseFromFile(String folder, String word) {

        try {

            File file = new File(new File(folder), getFileName(word));

            if (!file.exists()) {
                return null;
            }

            StringBuilder builder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
            }

            return builder.toString();

        } catch (Exception ex) {

            Toast.makeText(
                    OpenAiActivity.this,
                    ex.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            setTitle("Error");
        }
        return null;
    }

    final static String cacheKey = "AICACHE";
    private ArrayList<String> loadWordsFile(String folder, String fileName) {


        ArrayList<String> values = Cache.getInstance().getWordListItems(cacheKey);
        if(values != null && values.size() != 0) {
            return values;
        }

        values = new ArrayList<>();
        try {



            File file = new File(folder, fileName);

            if (!file.exists()) {
                return values;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    int index = line.indexOf('/');
                    if(index == -1) {
                        values.add(line.trim());
                    } else {
                        values.add(line.substring(0, index));
                    }

                }
            }

            Cache.getInstance().addWordListItems(cacheKey, values);
            return values;

        } catch (Exception ex) {

            Toast.makeText(
                    OpenAiActivity.this,
                    ex.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            setTitle("Error");
        }
        return values;
    }

    private String getFileName(String word) {
        return "gpt_" + word + ".txt";
   }
    private void onClear() {
        editPrompt.setText("");
    }

}