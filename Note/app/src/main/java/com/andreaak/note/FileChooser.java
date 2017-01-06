package com.andreaak.note;

import java.io.File;
import java.util.List;

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

public class FileChooser extends ListActivity {
    public static final String FILE_NAME = "FileName";
    public static final String PATH = "Path";

    private FileArrayAdapter adapter;
    private FilesHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File currentDir = Environment.getExternalStorageDirectory();
        helper = new FilesHelper(this);
        fill(currentDir);
    }

    private void fill(File file) {
        this.setTitle(file.getAbsolutePath());
        List<FileItem> dir = helper.getDirectory(file);
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            File currentDir = new File(item.getPath());
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
