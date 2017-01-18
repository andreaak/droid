package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.dataBase.EntityHelper;
import com.andreaak.note.dataBase.EntityItem;
import com.andreaak.note.dataBase.FindNoteArrayAdapter;
import com.andreaak.note.dataBase.FindNoteItem;
import com.andreaak.note.utils.Utils;

import java.util.List;

public class NoteFindActivity extends ListActivity {

    public static final String TEXT = "text";

    private FindNoteArrayAdapter adapter;
    private EntityHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
        String text = getIntent().getStringExtra(NoteFindActivity.TEXT);
        fill(text);
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (EntityHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            helper = new EntityHelper(this);
            if (!helper.openDatabase()) {
                Utils.showText(this, R.string.db_fault);
                finishWithFault();
                return;
            }
        }


        if (helper.getCurrentText() != null)
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
        List<FindNoteItem> dir = helper.findNotes(text);
        adapter = new FindNoteArrayAdapter(NoteFindActivity.this, R.layout.activity_note_find, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        EntityItem item = adapter.getItem(position);
        onNoteClick(item);
    }

    private void onNoteClick(EntityItem item) {
        Intent intent = new Intent(this, NoteHtmlActivity.class);
        intent.putExtra(NoteHtmlActivity.ID, item.getId());
        intent.putExtra(NoteHtmlActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
