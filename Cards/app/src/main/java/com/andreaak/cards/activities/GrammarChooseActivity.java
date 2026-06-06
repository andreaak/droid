package com.andreaak.cards.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.FileArrayAdapter;
import com.andreaak.cards.activities.helpers.FileHelper;
import com.andreaak.cards.activities.helpers.FileItem;
import com.andreaak.common.fileSystemItems.ItemType;

import java.util.List;

public class GrammarChooseActivity extends ListActivity {

    //in
    public static final String PATH = "path";

    private FileArrayAdapter adapter;
    private FileHelper helper;

    private Menu menu;

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
    protected void onRestart() {
        //googleDriveHelper.setActivity(this, operationGoogleDriveHelper);
        super.onRestart();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verb_choose, menu);
        //menu.setGroupVisible(com.andreaak.cards.R.id.groupGoogle, googleDriveHelper.isConnected());
        this.menu = menu;
        //operationGoogleDriveHelper.setMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fill(String currentPath) {
        List<FileItem> dir = helper.getEntities(currentPath);
        setTitle(helper.getDescriptions(currentPath));
        adapter = new FileArrayAdapter(GrammarChooseActivity.this, R.layout.activity_file_chooser, dir);
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
