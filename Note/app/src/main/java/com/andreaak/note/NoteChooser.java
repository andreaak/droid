package com.andreaak.note;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.utils.ItemType;
import com.andreaak.note.dataBase.NoteArrayAdapter;
import com.andreaak.note.dataBase.NoteItem;
import com.andreaak.note.dataBase.NoteHelper;

import java.util.List;

public class NoteChooser extends ListActivity {

    private NoteArrayAdapter adapter;
    private NoteHelper helper;
    private String path;
    private String fileName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getIntent().getStringExtra(FileChooser.PATH);
        fileName = getIntent().getStringExtra(FileChooser.FILE_NAME);
        helper = new NoteHelper(this, path, fileName);
        if(!helper.IsActive()) {
            finishWithFault();
            return;
        }
        fill(NoteHelper.ROOT);
    }

    private void finishWithFault() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void fill(int currentId) {
        List<NoteItem> dir = helper.getNoteItems(currentId);
        adapter = new NoteArrayAdapter(NoteChooser.this, R.layout.note_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        NoteItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory ||item.getType() == ItemType.ParentDirectory) {
            fill(item.getId());
        } else {
            onNoteClick(item);
        }
    }

    private void onNoteClick(NoteItem item) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(NoteActivity.ID, item.getId());
        intent.putExtra(NoteActivity.DESCRIPTION, item.getDescription());
        intent.putExtra(FileChooser.PATH, path);
        intent.putExtra(FileChooser.FILE_NAME, fileName);
        startActivity(intent);
    }
}
