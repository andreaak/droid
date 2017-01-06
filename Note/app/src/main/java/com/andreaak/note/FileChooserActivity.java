package com.andreaak.note;

import java.io.File;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.files.FileArrayAdapter;
import com.andreaak.note.files.FileItem;
import com.andreaak.note.files.FilesHelper;
import com.andreaak.note.utils.ItemType;

public class FileChooserActivity extends ListActivity {
    public static final String FILE_NAME = "FileName";
    public static final String PATH = "Path";
    public static final String SAVED_PATH = "saved_path";

    private FileArrayAdapter adapter;
    private FilesHelper helper;
    private File currentDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
        helper = new FilesHelper(this);
        fill(currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            SharedPreferences sPref = getPreferences(MODE_PRIVATE);
            String savedPath = sPref.getString(SAVED_PATH, "");

            currentDir = savedPath == null || savedPath.equals("") ?
                    Environment.getExternalStorageDirectory() :
                    new File(savedPath);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return currentDir;
    }

    private void fill(File file) {
        this.setTitle(file.getAbsolutePath());
        List<FileItem> dir = helper.getDirectory(file);
        adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.activity_file_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            currentDir = new File(item.getPath());
            fill(currentDir);
        } else {
            onFileClick(item);
        }
    }

    private void onFileClick(FileItem item) {
        Intent intent = new Intent();
        intent.putExtra(PATH, item.getPath());
        intent.putExtra(FILE_NAME, item.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
