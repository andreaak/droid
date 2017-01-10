package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.andreaak.note.dataBase.EntityArrayAdapter;
import com.andreaak.note.dataBase.EntityHelper;
import com.andreaak.note.dataBase.EntityItem;

import java.util.List;
import java.util.StringTokenizer;

public class NoteFindActivity extends ListActivity {

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
            helper = new EntityHelper(this);
        }
        if (!helper.openDatabase()) {
            Toast.makeText(this, "Database fault", Toast.LENGTH_LONG).show();
            finishWithFault();
            return;
        }

        if(helper.getCurrentText() != null)
            fill(helper.getCurrentText());
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

    private void fill(String text) {
        List<EntityItem> dir = helper.findNotes(text);
        adapter = new EntityArrayAdapter(NoteFindActivity.this, R.layout.activity_note_find, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        EntityItem item = adapter.getItem(position);
        onNoteClick(item);
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
