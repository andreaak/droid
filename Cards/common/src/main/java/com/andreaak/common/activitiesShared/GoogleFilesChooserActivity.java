package com.andreaak.common.activitiesShared;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.andreaak.common.R;
import com.andreaak.common.configs.Configs;
import com.andreaak.common.google.GoogleArrayAdapter;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.GoogleItem;
import com.andreaak.common.google.IGoogleSearch;
import com.andreaak.common.predicates.AlwaysTruePredicate;
import com.andreaak.common.predicates.DirectoryNamePredicate;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class GoogleFilesChooserActivity extends Activity implements View.OnClickListener, IGoogleSearch {

    //in
    public static final String PREDICATE = "Predicate";
    public static final String APP_NAME = "AppName";
    public static final String DOWNLOAD_TO_PATH_INITIAL = "DownloadToPathInitial";
    //out
    public static final String IDS = "ids";
    public static final String NAMES = "names";
    public static final String DOWNLOAD_TO_PATH = "DownloadToPath";

    public static final int REQUEST_DIRECTORY_CHOOSER = 4;

    private GoogleArrayAdapter adapter;
    private GoogleDriveHelper googleDriveHelper;

    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;
    private Button buttonSelectAll;
    private List<GoogleItem> databaseFiles;
    private List<GoogleItem> selectedFiles;

    private DirectoryNamePredicate predicate;
    private String appName;
    private String downloadToPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.andreaak.common.R.layout.shared_activity_google_files_chooser);

        listView = (ListView) findViewById(com.andreaak.common.R.id.lvMain);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttonOk = (Button) findViewById(com.andreaak.common.R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(com.andreaak.common.R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);
        buttonSelectAll = (Button) findViewById(com.andreaak.common.R.id.buttonSelectAll);
        buttonSelectAll.setOnClickListener(this);

        googleDriveHelper = GoogleDriveHelper.getInstance();
        RestoreInParameters();
        fill();
    }

    private void RestoreInParameters() {
        predicate = (DirectoryNamePredicate) getIntent().getSerializableExtra(PREDICATE);
        appName = (String) getIntent().getSerializableExtra(APP_NAME);
        downloadToPath = (String) getIntent().getSerializableExtra(DOWNLOAD_TO_PATH_INITIAL);
    }

    private void fill() {
        final boolean[] isDownload = {false};
        final IGoogleSearch act = this;
        databaseFiles = new ArrayList();
        setTitle(com.andreaak.common.R.string.search);

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    GoogleItem directory = googleDriveHelper.searchFolder("root", Configs.getInstance().GoogleDir, null);
                    if (directory != null) {
                        ArrayList<GoogleItem> findFiles = googleDriveHelper.search(directory.getId(), null, null);
                        for (GoogleItem file : findFiles) {
                            if (!googleDriveHelper.isFolder(file) && predicate.isValid(file.getTitle())) {
                                databaseFiles.add(file);
                            }
                        }
                    }
                    publishProgress("Search Completed");
                    isDownload[0] = true;
                } catch (Exception ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    return ex;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... strings) {
                super.onProgressUpdate(strings);
                Logger.d(Constants.LOG_TAG, strings[0]);
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    act.onSearchFinished(null);
                } else {
                    act.onSearchFinished(ex);
                }
            }
        }.execute();
    }

    @Override
    public void onSearchFinished(Exception ex) {
        if (ex == null) {
            adapter = new GoogleArrayAdapter(this, com.andreaak.common.R.layout.shared_list_item_google_files_chooser, databaseFiles);
            listView.setAdapter(adapter);
            selectNew();
        } else {
            Utils.showText(this, com.andreaak.common.R.string.search_fault);
        }
        setTitle(appName);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonSelectAll) {
            selectAll();

        } else if (id == R.id.buttonOk) {
            processSelection();

        } else if (id == R.id.buttonCancel) {
            onCancel();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    processSelection(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void selectAll() {

        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            listView.setItemChecked(i, true);
        }
    }

    private void selectNew() {

        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            GoogleItem item = (GoogleItem) adapter.getItem(i);
            if (item.isNew()) {
                listView.setItemChecked(i, true);
            }
        }
    }

    private void processSelection() {
        SparseBooleanArray sbArray = listView.getCheckedItemPositions();
        selectedFiles = new ArrayList<>();

        for (int i = 0; i < sbArray.size(); i++) {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key)) {
                GoogleItem file = databaseFiles.get(key);
                selectedFiles.add(file);
            }
        }
        if (Utils.isEmpty(downloadToPath)) {
            selectDownloadFolder();
        } else {
            processSelection(downloadToPath);
        }
    }

    private void selectDownloadFolder() {
        Intent intent = new Intent(this, DirectoryChooserActivity.class);
        intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
        intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_working_directory));
        intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, Configs.getInstance().WorkingDir);
        startActivityForResult(intent, REQUEST_DIRECTORY_CHOOSER);
    }

    private void processSelection(String downloadToPath) {

        String[] ids = new String[selectedFiles.size()];
        String[] names = new String[selectedFiles.size()];

        for (int i = 0; i < selectedFiles.size(); i++) {
            GoogleItem gi = selectedFiles.get(i);
            ids[i] = gi.getId();
            names[i] = gi.getTitle();
        }

        Intent intent = new Intent();
        intent.putExtra(IDS, ids);
        intent.putExtra(NAMES, names);
        intent.putExtra(DOWNLOAD_TO_PATH, downloadToPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}