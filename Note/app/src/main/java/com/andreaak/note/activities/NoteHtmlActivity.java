package com.andreaak.note.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.note.R;
import com.andreaak.note.configs.AppConfigs;
import com.andreaak.note.dataBase.EntityHelper;
import com.andreaak.note.dataBase.EntityItem;

import java.util.List;

import static com.andreaak.common.utils.Utils.getSeparatedText;

public class NoteHtmlActivity extends Activity {

    public static final String ID = "id";
    public static final String DESCRIPTION = "description";

    private WebView webView;
    private EntityHelper helper;

    private int[] backgrounds = {Color.WHITE
            ,  Color.parseColor("#f3f3f3")
            ,  Color.parseColor("#e9e9e9")
            ,  Color.parseColor("#dfdfdf")
            ,  Color.parseColor("#d6d6d6")
            ,  Color.parseColor("#cccccc")
            ,  Color.parseColor("#c2c2c2")
            ,  Color.parseColor("#b7b7b7")
            ,  Color.parseColor("#ababab")
            ,  Color.parseColor("#9e9e9e")
            ,  Color.parseColor("#919191")
            ,  Color.parseColor("#848484")
            ,  Color.parseColor("#767676")
            ,  Color.parseColor("#696969")
            ,  Color.parseColor("#5c5c5c")
            ,  Color.parseColor("#4f4f4f")
            ,  Color.parseColor("#434343")
            ,  Color.parseColor("#363636")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();

        setContentView(R.layout.activity_note_html);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        if(helper.getCurrentId() == -2){
            helper.setCurrentId(getIntent().getIntExtra(ID, -1));
        }

        if(helper.getCurrentId() != EntityHelper.ROOT) {
            loadText(helper.getCurrentId(), getSeparatedText("/", helper.getDescriptions(helper.getCurrentId())));
        }
    }

    private void onRestoreNonConfigurationInstance() {

        helper = (EntityHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            helper = new EntityHelper(this);
            helper.setCurrentId(-2);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
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
        } else if (item.getItemId() == R.id.menu_next) {
            next();
            return true;
        } else if (item.getItemId() == R.id.menu_previous) {
            previous();
            return true;
        } else if (item.getItemId() == R.id.menu_change_background) {
            changeBackground();
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

    private void next() {
        EntityItem entity = helper.getNextEntity(helper.getCurrentId());
        if(entity != null) {
            List<String> descriptions = helper.getDescriptions(entity.getId());
            helper.setCurrentId(entity.getId());
            loadText(entity.getId(), getSeparatedText("/", descriptions));
        }
    }

    private void previous() {
        EntityItem entity = helper.getPreviousEntity(helper.getCurrentId());
        if(entity != null) {
            List<String> descriptions = helper.getDescriptions(entity.getId());
            helper.setCurrentId(entity.getId());
            loadText(entity.getId(), getSeparatedText("/", descriptions));
        }
    }

    private void changeBackground() {

        int currentBackground = SharedPreferencesHelper.getInstance().getInt(AppConfigs.SP_TEXT_BACK);
        if (currentBackground == SharedPreferencesHelper.NOT_DEFINED_INT) {
            currentBackground = 0;
        } else {
            currentBackground++;
            if(currentBackground >= backgrounds.length) {
                currentBackground = 0;
            }
        }

        webView.setBackgroundColor(backgrounds[currentBackground]);
        saveBackground(currentBackground);
    }

    private void loadText(int id, String description) {
        int zoom = SharedPreferencesHelper.getInstance().getInt(AppConfigs.SP_TEXT_ZOOM);
        if (zoom != SharedPreferencesHelper.NOT_DEFINED_INT) {
            webView.getSettings().setTextZoom(zoom);
        }

        int currentBackground = SharedPreferencesHelper.getInstance().getInt(AppConfigs.SP_TEXT_BACK);
        if (currentBackground != SharedPreferencesHelper.NOT_DEFINED_INT) {
            webView.setBackgroundColor(backgrounds[currentBackground]);
        }

        setTitle(description);

        String text = helper.getEntityDataHtml(id);

        webView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
        webView.scrollTo(0, 0);
    }

    private void saveTextZoom(int textZoom) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_TEXT_ZOOM, textZoom);
    }

    private void saveBackground(int index) {
        SharedPreferencesHelper.getInstance().save(AppConfigs.SP_TEXT_BACK, index);
    }
}
