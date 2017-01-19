package com.andreaak.note;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.andreaak.note.files.DirectoriesHelper;
import com.andreaak.note.files.DirectoryArrayAdapter;
import com.andreaak.note.files.FileItem;
import com.andreaak.note.utils.Configs;
import com.andreaak.note.utils.ItemType;
import com.andreaak.note.utils.SharedPreferencesHelper;

import java.io.File;
import java.util.List;

public class DirectoryChooserActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    public static final String PATH = "Path";

    private DirectoryArrayAdapter adapter;
    private DirectoriesHelper helper;
    private File currentDir;
    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_chooser);

        listView = (ListView) findViewById(R.id.lvMain);
        listView.setOnItemClickListener(this);
        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        onRestoreNonConfigurationInstance();
        helper = new DirectoriesHelper(this);
        fill(currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            String savedPath = SharedPreferencesHelper.getInstance().read(Configs.SP_DOWNLOAD_DIR_PATH);
            currentDir = savedPath.equals("") || !new File(savedPath).exists() ?
                    Environment.getExternalStorageDirectory() :
                    new File(savedPath);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return currentDir;
    }

    private void fill(File file) {
        this.setTitle(file.getAbsolutePath());
        List<FileItem> dir = helper.getDirectory(file);
        adapter = new DirectoryArrayAdapter(this, R.layout.list_item_dir_chooser, dir);
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            currentDir = new File(item.getPath());
            fill(currentDir);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonOk:
                onOkClick(helper.getCurrentDirectory());
                break;
            case R.id.buttonCancel:
                onCancel();
                break;
        }
    }

    private void onOkClick(FileItem item) {
        SharedPreferencesHelper.getInstance().save(Configs.SP_DOWNLOAD_DIR_PATH, item.getPath());

        Intent intent = new Intent();
        intent.putExtra(PATH, item.getPath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
