package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.adapters.FileArrayAdapter;
import com.andreaak.note.adapters.Item;
import com.andreaak.note.dataBase.DataBaseHelper;
import com.andreaak.note.dataBase.NoteHelper;
import com.andreaak.note.files.FilesHelper;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteChooser extends ListActivity {

    private FileArrayAdapter adapter;
    private NoteHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(FileChooser.PATH);
        String fileName = getIntent().getStringExtra(FileChooser.FILE_NAME);
        helper = new NoteHelper(this, path, fileName);
        if(!helper.IsActive()) {
            finishWithFault();
            return;
        }
        fill();
    }

    private void finishWithFault() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void fill() {
        List<Item> dir = helper.getDirectory();
        adapter = new FileArrayAdapter(NoteChooser.this, R.layout.note_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getImage() == R.drawable.directory_icon || o.getImage() == R.drawable.directory_up) {
            //currentDir = new File(o.getPath());
            //fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
//        intent.putExtra(PATH, currentDir.toString());
//        intent.putExtra(FILE_NAME, o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
