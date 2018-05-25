package com.andreaak.common.activitiesShared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.andreaak.common.R;
import com.andreaak.common.fileSystemItems.DirectoryArrayAdapter;
import com.andreaak.common.fileSystemItems.FileItem;
import com.andreaak.common.fileSystemItems.FilesHelper;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.predicates.AlwaysTruePredicate;
import com.andreaak.common.predicates.DirectoryPredicate;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.util.List;

public class DirectoryChooserActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    //in
    public static final String PREDICATE = "Predicate";
    public static final String TITLE = "Title";
    public static final String INITIAL_PATH = "InitialPath";
    //out
    public static final String DIRECTORY_PATH = "DirectoryPath";

    private DirectoryPredicate predicate;
    private String title;

    private DirectoryArrayAdapter adapter;
    private FilesHelper helper;
    private File currentDir;
    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;
    private TextView textViewPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.andreaak.common.R.layout.shared_activity_dir_chooser);

        listView = (ListView) findViewById(com.andreaak.common.R.id.lvMain);
        listView.setOnItemClickListener(this);
        buttonOk = (Button) findViewById(com.andreaak.common.R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(com.andreaak.common.R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);
        textViewPath = (TextView) findViewById(com.andreaak.common.R.id.textViewPath);


        onRestoreNonConfigurationInstance();
        helper = new FilesHelper(this, false);
        fill(currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        currentDir = (File) getLastNonConfigurationInstance();
        if (currentDir == null) {
            RestoreInParameters();
        }
    }

    private void RestoreInParameters() {
        String savedPath = getIntent().getStringExtra(INITIAL_PATH);
        currentDir = Utils.isEmpty(savedPath) || !new File(savedPath).exists() ?
                Environment.getDataDirectory() :
                new File(savedPath);
        predicate = (DirectoryPredicate) getIntent().getSerializableExtra(PREDICATE);
        title = getIntent().getStringExtra(TITLE);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return currentDir;
    }

    private void fill(File file) {
        setCustomTitle(file);
        List<FileItem> dir = helper.getDirectory(file, new AlwaysTruePredicate());
        adapter = new DirectoryArrayAdapter(this, com.andreaak.common.R.layout.shared_list_item_dir_chooser, dir);
        listView.setAdapter(adapter);
        setOkButtonState(file);
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
        if (id == R.id.buttonOk) {
            onOkClick(helper.getCurrentDirectory());

        } else if (id == R.id.buttonCancel) {
            onCancel();

        }
    }

    private void onOkClick(FileItem item) {

        Intent intent = new Intent();
        intent.putExtra(DIRECTORY_PATH, item.getPath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void setOkButtonState(File directory) {
        boolean isEnabled = (predicate == null || predicate.isValid(directory));
        buttonOk.setEnabled(isEnabled);
    }

    private void setCustomTitle(File directory) {
        if (title != null) {
            this.setTitle(title);
        } else {
            this.setTitle(getString(R.string.select_directory));
        }

        textViewPath.setText(directory.getAbsolutePath());
    }
}
