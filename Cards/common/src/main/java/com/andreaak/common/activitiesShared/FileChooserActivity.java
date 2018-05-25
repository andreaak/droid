package com.andreaak.common.activitiesShared;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.common.fileSystemItems.FileArrayAdapter;
import com.andreaak.common.fileSystemItems.FileItem;
import com.andreaak.common.fileSystemItems.FilesHelper;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.predicates.DirectoryPredicate;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.util.List;

public class FileChooserActivity extends ListActivity {

    //in
    public static final String PREDICATE = "Predicate";
    public static final String TITLE = "Title";
    public static final String INITIAL_PATH = "InitialPath";
    //out
    public static final String FILE_NAME = "FileName";
    public static final String FILE_PATH = "FilePath";

    private DirectoryPredicate predicate;
    private String title;

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
            RestoreInParameters();
        }
    }

    private void RestoreInParameters() {
        String savedPath = getIntent().getStringExtra(INITIAL_PATH);
        predicate = (DirectoryPredicate) getIntent().getSerializableExtra(PREDICATE);
        title = getIntent().getStringExtra(TITLE);

        currentDir = Utils.isEmpty(savedPath) || !new File(savedPath).exists() ?
                Environment.getDataDirectory() :
                new File(savedPath);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return currentDir;
    }

    private void fill(File file) {
        this.setTitle(file.getAbsolutePath());
        List<FileItem> dir = helper.getDirectory(file, predicate);
        adapter = new FileArrayAdapter(FileChooserActivity.this, com.andreaak.common.R.layout.shared_list_item_file_chooser, dir);
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
        intent.putExtra(FILE_PATH, item.getPath());
        intent.putExtra(FILE_NAME, item.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
