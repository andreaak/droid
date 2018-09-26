package com.andreaak.cards.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.FileArrayAdapter;
import com.andreaak.cards.activities.helpers.FileHelper;
import com.andreaak.cards.activities.helpers.FileItem;
import com.andreaak.common.fileSystemItems.ItemType;

import java.util.List;

public class FileChooserActivity extends ListActivity {

    public static final String PATH = "path";

    private FileArrayAdapter adapter;
    private FileHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (FileHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            String path = getIntent().getStringExtra(PATH);
            helper = new FileHelper(this, path);
        }

        fill(helper.getCurrentPath());
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void fill(String currentPath) {
        List<FileItem> dir = helper.getEntities(currentPath);
        setTitle(helper.getDescriptions(currentPath));
        adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.activity_file_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            fill(item.getPath());
        } else {
            onNoteClick(item);
        }
    }

    private void onNoteClick(FileItem item) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra(HtmlActivity.PATH, item.getPath());
        intent.putExtra(HtmlActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
