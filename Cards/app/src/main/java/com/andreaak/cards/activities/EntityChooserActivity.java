package com.andreaak.cards.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.andreaak.cards.R;
import com.andreaak.cards.activities.helpers.EntityArrayAdapter;
import com.andreaak.cards.activities.helpers.EntityHelper;
import com.andreaak.cards.activities.helpers.EntityItem;
import com.andreaak.common.fileSystemItems.ItemType;

import java.util.List;

public class EntityChooserActivity extends ListActivity {

    public static final String PATH = "path";

    private EntityArrayAdapter adapter;
    private EntityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (EntityHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            String path = getIntent().getStringExtra(PATH);
            helper = new EntityHelper(this, path);
        }

        fill(helper.getCurrentPath());
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void fill(String currentPath) {
        List<EntityItem> dir = helper.getEntities(currentPath);
        setTitle(helper.getDescriptions(currentPath));
        adapter = new EntityArrayAdapter(EntityChooserActivity.this, R.layout.activity_entity_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        EntityItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            fill(item.getPath());
        } else {
            onNoteClick(item);
        }
    }

    private void onNoteClick(EntityItem item) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra(HtmlActivity.PATH, item.getPath());
        intent.putExtra(HtmlActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
