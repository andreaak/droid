package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.files.FileArrayAdapter;
import com.andreaak.note.files.FileItem;
import com.andreaak.note.files.FilesHelper;
import com.andreaak.note.utils.Configs;
import com.andreaak.note.utils.ItemType;
import com.andreaak.note.utils.SharedPreferencesHelper;

import java.io.File;
import java.util.List;

public class FileChooserActivity extends ListActivity {
    public static final String FILE_NAME = "FileName";
    public static final String PATH = "Path";

    private FileArrayAdapter adapter;
    private FilesHelper helper;
    private File currentDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
        helper = new FilesHelper(this, true);
        fill(currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            String savedPath = SharedPreferencesHelper.getInstance().getString(Configs.SP_DIRECTORY_WITH_DB_PATH);
            currentDir = savedPath.equals("") || !new File(savedPath).exists() ?
                    Environment.getDataDirectory() :
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
