package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.andreaak.note.utils.ItemType;
import com.andreaak.note.dataBase.NoteArrayAdapter;
import com.andreaak.note.dataBase.NoteItem;
import com.andreaak.note.dataBase.NoteHelper;

import java.util.List;

public class NoteChooserActivity extends ListActivity {

    private NoteArrayAdapter adapter;
    private NoteHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRestoreNonConfigurationInstance();
    }

    private void onRestoreNonConfigurationInstance() {
        helper = (NoteHelper) getLastNonConfigurationInstance();
        if (helper == null) {
            helper = new NoteHelper(this);
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

    private void fill(int currentId) {
        List<NoteItem> dir = helper.getNoteItems(currentId);
        adapter = new NoteArrayAdapter(NoteChooserActivity.this, R.layout.activity_note_chooser, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        NoteItem item = adapter.getItem(position);
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

    private void onNoteClick(NoteItem item) {
        Intent intent = new Intent(this, NoteTextActivity.class);
        intent.putExtra(NoteTextActivity.ID, item.getId());
        intent.putExtra(NoteTextActivity.DESCRIPTION, item.getDescription());
        startActivity(intent);
    }
}
