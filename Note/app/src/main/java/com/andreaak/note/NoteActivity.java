package com.andreaak.note;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.andreaak.note.dataBase.DataBaseHelper;

public class NoteActivity extends Activity {

    public static final String ID = "id";
    public static final String DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        loadText();

    }

    private void loadText() {

        String description = getIntent().getStringExtra(DESCRIPTION);
        setTitle(description);

        int id = getIntent().getIntExtra(ID, -1);
        String path = getIntent().getStringExtra(FileChooser.PATH);
        String fileName = getIntent().getStringExtra(FileChooser.FILE_NAME);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this, path, fileName);
        dataBaseHelper.openDataBase();
        String text = dataBaseHelper.GetEntityData(id);
        Spanned sp =  Html.fromHtml(text);
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(text);
    }
}
