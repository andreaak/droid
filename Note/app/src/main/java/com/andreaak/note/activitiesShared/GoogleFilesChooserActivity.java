package com.andreaak.note.activitiesShared;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.andreaak.note.R;
import com.andreaak.note.configs.Configs;
import com.andreaak.note.google.GoogleArrayAdapter;
import com.andreaak.note.google.GoogleDriveHelper;
import com.andreaak.note.google.GoogleItem;
import com.andreaak.note.google.IGoogleSearch;
import com.andreaak.note.predicates.AlwaysTruePredicate;
import com.andreaak.note.predicates.DirectoryNamePredicate;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.Utils;
import com.andreaak.note.utils.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class GoogleFilesChooserActivity extends Activity implements View.OnClickListener, IGoogleSearch {

    //in
    public static final String PREDICATE = "Predicate";
    //out
    public static final String IDS = "ids";
    public static final String NAMES = "names";
    public static final String PATH = "path";

    public static final int REQUEST_DIRECTORY_CHOOSER = 4;

    private GoogleArrayAdapter adapter;
    private GoogleDriveHelper helper;

    private ListView listView;
    private Button buttonOk;
    private Button buttonCancel;
    private List<GoogleItem> databaseFiles;
    private List<GoogleItem> selectedFiles;

    private DirectoryNamePredicate predicate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.andreaak.note.R.layout.activity_google_files_chooser);

        listView = (ListView) findViewById(com.andreaak.note.R.id.lvMain);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttonOk = (Button) findViewById(com.andreaak.note.R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(com.andreaak.note.R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        helper = GoogleDriveHelper.getInstance();
        RestoreInParameters();
        fill();
    }

    private void RestoreInParameters() {
        predicate = (DirectoryNamePredicate) getIntent().getSerializableExtra(PREDICATE);
    }

    private void fill() {
        final boolean[] isDownload = {false};
        final IGoogleSearch act = this;
        databaseFiles = new ArrayList();
        setTitle(com.andreaak.note.R.string.search);

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    List<GoogleItem> directory = helper.search("root", Configs.GoogleDir, null);
                    if (directory != null && directory.size() == 1) {
                        ArrayList<GoogleItem> findFiles = helper.search(directory.get(0).getId(), null, null);
                        for (GoogleItem file : findFiles) {
                            if (!helper.isFolder(file) && predicate.isValid(file.getTitle())) {
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
            adapter = new GoogleArrayAdapter(this, com.andreaak.note.R.layout.shared_list_item_google_files_chooser, databaseFiles);
            listView.setAdapter(adapter);
        } else {
            Utils.showText(this, com.andreaak.note.R.string.search_fault);
        }
        setTitle(com.andreaak.note.R.string.app_name);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case com.andreaak.note.R.id.buttonOk:
                ProcessSelection();
                break;
            case com.andreaak.note.R.id.buttonCancel:
                onCancel();
                break;
        }
    }

    private void ProcessSelection() {
        SparseBooleanArray sbArray = listView.getCheckedItemPositions();
        selectedFiles = new ArrayList<>();

        for (int i = 0; i < sbArray.size(); i++) {
            int key = sbArray.keyAt(i);
            if (sbArray.get(key)) {
                GoogleItem file = databaseFiles.get(key);
                selectedFiles.add(file);
            }
        }
        getDirectory();
    }

    private void getDirectory() {

        String initialPath = Configs.FilesDir;
        if (Utils.isEmpty(initialPath)) {
            Intent intent = new Intent(this, DirectoryChooserActivity.class);
            intent.putExtra(DirectoryChooserActivity.PREDICATE, new AlwaysTruePredicate());
            intent.putExtra(DirectoryChooserActivity.TITLE, getString(R.string.select_directory));
            intent.putExtra(DirectoryChooserActivity.INITIAL_PATH, initialPath);
            startActivityForResult(intent, REQUEST_DIRECTORY_CHOOSER);
        } else {
            onFolderSelected(initialPath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.DIRECTORY_PATH);
                    onFolderSelected(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onFolderSelected(String path) {

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
        intent.putExtra(PATH, path);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onCancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}