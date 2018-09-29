package com.andreaak.cards.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;

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
        float scale = AppConfigs.getInstance().Scale;
        webView.setInitialScale((int) (scale * 100));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_html, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_plus) {
            webView.zoomIn();
            AppConfigs.getInstance().saveScale(webView.getScale());
            return true;
        } else if (item.getItemId() == R.id.menu_minus) {
            webView.zoomOut();
            AppConfigs.getInstance().saveScale(webView.getScale());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadText() {
        String description = getIntent().getStringExtra(DESCRIPTION);
        setTitle(description);

        String path = getIntent().getStringExtra(PATH);

        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        //String text = FilesHelper.getTextFileContent(path);
        // webView.loadData(text, "text/html; charset=utf-8", "utf-8");
        //webView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        String url = "file://" + path;
        webView.loadUrl(url);
    }
}
