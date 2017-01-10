package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreaak.note.utils.ItemType;
import com.andreaak.note.dataBase.EntityArrayAdapter;
import com.andreaak.note.dataBase.EntityItem;
import com.andreaak.note.dataBase.EntityHelper;

import java.util.List;

public class EntityChooserActivity extends ListActivity {

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
            Toast.makeText(this, "Database fault", Toast.LENGTH_LONG).show();
            finishWithFault();
            return;
        }
        fill(helper.getCurrentId());
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return helper;
    }

    private void finishWithFault() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void SetTilte(List<String> descriptions) {
        if (descriptions.isEmpty()) {
            setTitle(getString(R.string.app_name));
            return;
        }
        setTitle(Constants.getText("/", descriptions));
    }

    private void fill(int currentId) {
        List<EntityItem> dir = helper.getEntities(currentId);
        SetTilte(helper.getDescriptions(currentId));
        adapter = new EntityArrayAdapter(EntityChooserActivity.this, R.layout.activity_entity_chooser, dir);
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
        //Intent intent = new Intent(this, NoteTextActivity.class);
        Intent intent = new Intent(this, NoteHtmlActivity.class);
        intent.putExtra(NoteTextActivity.ID, item.getId());
        intent.putExtra(NoteTextActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
