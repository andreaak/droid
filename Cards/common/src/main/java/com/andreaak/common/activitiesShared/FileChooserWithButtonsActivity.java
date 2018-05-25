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
import com.andreaak.common.activitiesShared.helpers.FileChooserActivityHelper;
import com.andreaak.common.fileSystemItems.FileArrayAdapter;
import com.andreaak.common.fileSystemItems.FileItem;
import com.andreaak.common.fileSystemItems.FilesHelper;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.predicates.DirectoryPredicate;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.util.List;

public class FileChooserWithButtonsActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener {

    //in
    public static final String PREDICATE = "Predicate";
    public static final String TITLE = "Title";
    public static final String INITIAL_PATH = "InitialPath";
    //out
    public static final String FILE_NAME = "FileName";
    public static final String FILE_PATH = "FilePath";

    private FileArrayAdapter adapter;
    private FilesHelper helper;

    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;
    private TextView textViewPath;

    FileChooserActivityHelper activityHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_activity_file_chooser);

        listView = (ListView) findViewById(R.id.lvMain);
        listView.setOnItemClickListener(this);
        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);
        textViewPath = (TextView) findViewById(R.id.textViewPath);


        onRestoreNonConfigurationInstance();
        helper = new FilesHelper(this, true);
        fill(activityHelper.currentDir);
    }

    private void onRestoreNonConfigurationInstance() {
        activityHelper = (FileChooserActivityHelper) getLastNonConfigurationInstance();
        if (activityHelper == null) {
            activityHelper = new FileChooserActivityHelper();
            RestoreInParameters();
        }
    }

    private void RestoreInParameters() {
        String savedPath = getIntent().getStringExtra(INITIAL_PATH);
        activityHelper.predicate = (DirectoryPredicate) getIntent().getSerializableExtra(PREDICATE);
        activityHelper.title = getIntent().getStringExtra(TITLE);

        activityHelper.currentDir = Utils.isEmpty(savedPath) || !new File(savedPath).exists() ?
                Environment.getDataDirectory() :
                new File(savedPath);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return activityHelper;
    }

    private void fill(File file) {

        setCustomTitle(file);

        List<FileItem> dir = helper.getDirectory(file, activityHelper.predicate);
        adapter = new FileArrayAdapter(this, com.andreaak.common.R.layout.shared_list_item_file_chooser, dir);
        listView.setAdapter(adapter);

        setOkButtonState(file);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            activityHelper.currentDir = new File(item.getPath());
            fill(activityHelper.currentDir);
        } else {
            setOkButtonState(item);
            view.setSelected(true);
            activityHelper.currentPosition = position;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonOk) {
            FileItem item = adapter.getItem(activityHelper.currentPosition);
            if (item != null) {
                onOkClick(item);
            }

        } else if (id == R.id.buttonCancel) {
            onCancel();

        }
    }

    private void onOkClick(FileItem item) {

        Intent intent = new Intent();
        intent.putExtra(FILE_PATH, item.getPath());
        intent.putExtra(FILE_NAME, item.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void setOkButtonState(File directory) {
        boolean isEnabled = (activityHelper.predicate == null || activityHelper.predicate.isValid(directory));
        buttonOk.setEnabled(isEnabled);
    }

    private void setOkButtonState(FileItem file) {
        setOkButtonState(new File(file.getPath()));
    }

    private void setCustomTitle(File directory) {
        if (activityHelper.title != null) {
            this.setTitle(activityHelper.title);
        } else {
            this.setTitle(getString(R.string.select_directory));
        }

        textViewPath.setText(directory.getAbsolutePath());
    }
}
