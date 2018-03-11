package com.andreaak.cards;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.andreaak.cards.dataBase.EntityArrayAdapter;
import com.andreaak.cards.dataBase.EntityHelper;
import com.andreaak.cards.dataBase.EntityItem;
import com.andreaak.cards.utils.ItemType;

import java.util.List;

import static com.andreaak.cards.utils.Utils.getSeparatedText;
import static com.andreaak.cards.utils.Utils.showText;

public class EntityChooserActivity extends ListActivity implements SearchView.OnQueryTextListener {

    private EntityArrayAdapter adapter;
    private EntityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();

        int titleId = Resources.getSystem()
                .getIdentifier("action_bar_title", "id", "android");
        if (titleId > 0) {
            TextView title = (TextView) findViewById(titleId);
            title.setSingleLine(false);
            title.setMaxLines(2);
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        }
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (EntityHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            helper = new EntityHelper(this);
        }
        if (!helper.openDatabase()) {
            showText(this, com.andreaak.cards.R.string.db_fault);
            finishWithFault();
            return;
        }
        fill(helper.getCurrentId());
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.andreaak.cards.R.menu.menu_entity_chooser, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(com.andreaak.cards.R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onFindClick(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    private void finishWithFault() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void SetTilte(List<String> descriptions) {
        if (descriptions.isEmpty()) {
            setTitle(getString(com.andreaak.cards.R.string.app_name));
            return;
        }
        setTitle(getSeparatedText("/", descriptions));
    }

    private void fill(int currentId) {
        List<EntityItem> dir = helper.getEntities(currentId);
        SetTilte(helper.getDescriptions(currentId));
        adapter = new EntityArrayAdapter(EntityChooserActivity.this, com.andreaak.cards.R.layout.activity_entity_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        EntityItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            fill(item.getId());
        } else {
            onNoteClick(item);
        }
    }

    @Override
    public void onDestroy() {
        helper.close();
        super.onDestroy();
    }

    private void onNoteClick(EntityItem item) {
        Intent intent = new Intent(this, NoteHtmlActivity.class);
        intent.putExtra(NoteHtmlActivity.ID, item.getId());
        intent.putExtra(NoteHtmlActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }

    private void onFindClick(String text) {
        Intent intent = new Intent(this, NoteFindActivity.class);
        intent.putExtra(NoteFindActivity.TEXT, text);
        startActivity(intent);
    }
}
