package com.andreaak.note;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.andreaak.note.dataBase.DataBaseHelper;

public class NoteTextActivity extends Activity {

    public static final String ID = "id";
    public static final String DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_text);
        loadText();
    }

    private void loadText() {

        String description = getIntent().getStringExtra(DESCRIPTION);
        setTitle(description);

        int id = getIntent().getIntExtra(ID, -1);
        DataBaseHelper dataBaseHelper = DataBaseHelper.getInstance();
        TextView textview = (TextView) findViewById(R.id.textView);
        textview.setMovementMethod(new ScrollingMovementMethod());
//        String text = dataBaseHelper.GetEntityData(id);
        String text = dataBaseHelper.GetEntityDataHtml(id);
        Spanned value = Html.fromHtml(text);
        textview.setText(value);
    }
}
