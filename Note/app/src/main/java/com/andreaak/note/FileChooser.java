package com.andreaak.note;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.adapters.FileArrayAdapter;
import com.andreaak.note.adapters.Item;
import com.andreaak.note.files.FilesHelper;

public class FileChooser extends ListActivity {

    private static final String ROOT_DIRECTORY = "";
    private static final String PARENT_DIRECTORY_NAME = " ";
    private static final String PARENT_DIRECTORY_DATA = "..";
    public static final String FILE_NAME = "FileName";
    public static final String PATH = "Path";
    private File sdCard = Environment.getExternalStorageDirectory();
    private File currentDir;
    private FileArrayAdapter adapter;
    private FilesHelper helper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = sdCard;//new File("/" + ROOT_DIRECTORY + "/");
        helper = new FilesHelper(this);
        fill(currentDir);
    }

    private void fill(File f) {
        this.setTitle(f.getAbsolutePath());
        List<Item> dir = helper.getDirectory(f);
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getImage() == R.drawable.directory_icon || o.getImage() == R.drawable.directory_up) {
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra(PATH, currentDir.toString());
        intent.putExtra(FILE_NAME, o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
