package com.andreaak.cards.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.andreaak.cards.R;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HtmlActivity extends HandleExceptionActivity {

    public static final String PATH = "path";
    public static final String DESCRIPTION = "description";

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        loadText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_html, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_plus) {
            textBigger();
            return true;
        } else if (item.getItemId() == R.id.menu_minus) {
            textSmaller();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void textSmaller() {

        WebSettings settings = webView.getSettings();
        settings.setTextZoom(settings.getTextZoom() - 10);
        saveTextZoom(settings.getTextZoom());
    }

    private void textBigger() {

        WebSettings settings = webView.getSettings();
        settings.setTextZoom(settings.getTextZoom() + 10);
        saveTextZoom(settings.getTextZoom());
    }

    private void loadText() {
//        int zoom = SharedPreferencesHelper.getInstance().getInt(AppConfigs.SP_TEXT_ZOOM);
//        if (zoom != SharedPreferencesHelper.NOT_DEFINED_INT) {
//            webView.getSettings().setTextZoom(zoom);
//        }

        String description = getIntent().getStringExtra(DESCRIPTION);
        setTitle(description);

        String path = getIntent().getStringExtra(PATH);
        String text = getTextFileContent(path);
        webView.loadData(text, "text/html; charset=UTF-8", null);
    }

    private void saveTextZoom(int textZoom) {
//        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_TEXT_ZOOM, textZoom);
    }

    private String getTextFileContent(String path) {
        // This will reference one line at a time
        String line = null;
        StringBuilder sb = new StringBuilder();

        BufferedReader bufferedReader = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(path);

            // Always wrap FileReader in BufferedReader.
            bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
        } finally {
            // Always close files.
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                }
            }
        }

        return sb.toString();
    }
}
