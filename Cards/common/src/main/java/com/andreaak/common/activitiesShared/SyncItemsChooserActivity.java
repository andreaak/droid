package com.andreaak.common.activitiesShared;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.andreaak.common.R;
import com.andreaak.common.configs.Configs;
import com.andreaak.common.fileSystemItems.ItemType;
import com.andreaak.common.google.GoogleDriveHelper;
import com.andreaak.common.google.IGoogleSearch;
import com.andreaak.common.google.ISyncItem;
import com.andreaak.common.google.RootSyncItem;
import com.andreaak.common.google.SyncArrayAdapter;
import com.andreaak.common.google.SyncItem;
import com.andreaak.common.predicates.AlwaysTruePredicate;
import com.andreaak.common.predicates.DirectoryNamePredicate;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

public class SyncItemsChooserActivity extends Activity implements View.OnClickListener, ListView.OnItemClickListener, IGoogleSearch {

    //in
    public static final String PREDICATE = "Predicate";
    public static final String TITLE = "Title";
    public static final String ROOT_FOLDER = "RootFolder";
    public static final String REMOTE_ROOT_FOLDER = "RemoteRootFolder";
    public static final String DOWNLOAD_TO_PATH_INITIAL = "DownloadToPathInitial";
    //out
    public static final String ITEMS = "Items";
    public static final String DOWNLOAD_TO_PATH = "DownloadToPath";

    public static final int REQUEST_DIRECTORY_CHOOSER = 4;

    private SyncArrayAdapter adapter;
    private GoogleDriveHelper googleDriveHelper;

    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;
    private Button buttonSelectAll;

    private ISyncItem currentItem;

    private DirectoryNamePredicate predicate;
    private String title;

    private RootSyncItem rootSyncItem;
    private String downloadToPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_activity_google_files_chooser);

        listView = (ListView) findViewById(R.id.lvMain);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);
        buttonSelectAll = (Button) findViewById(R.id.buttonSelectAll);
        buttonSelectAll.setOnClickListener(this);

        googleDriveHelper = GoogleDriveHelper.getInstance();
        RestoreInParameters();
        rootSyncItem.item.init();
        fill(rootSyncItem);
    }

    private void RestoreInParameters() {
        predicate = (DirectoryNamePredicate) getIntent().getSerializableExtra(PREDICATE);
        title = getIntent().getStringExtra(TITLE);
        rootSyncItem = new RootSyncItem(getIntent().getStringExtra(ROOT_FOLDER),
                                        getIntent().getStringExtra(REMOTE_ROOT_FOLDER));
        downloadToPath = getIntent().getStringExtra(DOWNLOAD_TO_PATH_INITIAL);
    }

    @SuppressLint("StaticFieldLeak")
    private void fill(final ISyncItem item) {
        final boolean[] isDownload = {false};
        final IGoogleSearch act = this;
        setTitle(R.string.search);

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    item.init();
                    currentItem = item;
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
            adapter = new SyncArrayAdapter(this, R.layout.shared_list_item_file_chooser, currentItem.getItems());
            listView.setAdapter(adapter);
            selectNew();
        } else {
            Utils.showText(this, R.string.search_fault);
        }
        setTitle(title);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SyncItem item = adapter.getItem(position);
        if (item.getType() == ItemType.Directory || item.getType() == ItemType.ParentDirectory) {
            //activityHelper.currentDir = new File(item.getPath());
            fill(item);
        } else {
            //setOkButtonState(item);
            view.setSelected(true);
            //activityHelper.currentPosition = position;
        }
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
            SyncItem item = (SyncItem) adapter.getItem(i);
            if(item.getType() == ItemType.File) {
                item.setSelected(true);
                listView.setItemChecked(i, true);
            }
        }
    }

    private void selectNew() {

        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            SyncItem item = (SyncItem) adapter.getItem(i);
            if (item.isNew()) {
                item.setSelected(true);
                listView.setItemChecked(i, true);
            }
        }
    }

    private void processSelection() {

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

        Intent intent = new Intent();
        intent.putExtra(ITEMS, rootSyncItem);
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