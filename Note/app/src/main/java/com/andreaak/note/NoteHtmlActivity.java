package com.andreaak.note;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.utils.SharedPreferencesHelper;

public class NoteHtmlActivity extends Activity {

    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    private static final String TEXT_ZOOM = "TEXT_ZOOM";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_html);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        loadText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
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
        int zoom = SharedPreferencesHelper.getInstance().readInt(TEXT_ZOOM);
        if (zoom != SharedPreferencesHelper.NOT_DEFINED_INT) {
            webView.getSettings().setTextZoom(zoom);
        }

        String description = getIntent().getStringExtra(DESCRIPTION);
        setTitle(description);

        int id = getIntent().getIntExtra(ID, -1);
        DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance();
        String text = dataBaseHelper.GetEntityDataHtml(id);
        webView.loadData(text, "text/html; charset=UTF-8", null);
    }

    private void saveTextZoom(int textZoom) {
        SharedPreferencesHelper.getInstance().save(TEXT_ZOOM, textZoom);
    }
}
