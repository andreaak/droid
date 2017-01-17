package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.files.DirectoriesHelper;
import com.andreaak.note.files.FileArrayAdapter;
import com.andreaak.note.files.FileItem;
import com.andreaak.note.utils.ItemType;
import com.andreaak.note.utils.SharedPreferencesHelper;

import java.io.File;
import java.util.List;

public class DirectoryChooserActivity extends ListActivity {

    public static final String PATH = "Path";
    public static final String DOWNLOAD_DIR_PATH = "DOWNLOAD_DIR_PATH";

    private FileArrayAdapter adapter;
    private DirectoriesHelper helper;
    private File currentDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
        helper = new DirectoriesHelper(this);
        fill(currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            String savedPath = SharedPreferencesHelper.getInstance().read(DOWNLOAD_DIR_PATH);
            currentDir = savedPath.equals("") || !new File(savedPath).exists() ?
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
        adapter = new FileArrayAdapter(DirectoryChooserActivity.this, R.layout.activity_dir_chooser, dir);
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
        setResult(RESULT_OK, intent);
        finish();
    }
}
