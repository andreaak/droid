package com.andreaak.cards;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.andreaak.cards.google.GoogleArrayAdapter;
import com.andreaak.cards.google.GoogleDriveHelper;
import com.andreaak.cards.google.GoogleItem;
import com.andreaak.cards.utils.Configs;
import com.andreaak.cards.utils.Constants;
import com.andreaak.cards.utils.Utils;
import com.andreaak.cards.utils.logger.Logger;
import com.andreaak.cards.google.IGoogleSearch;

import java.util.ArrayList;
import java.util.List;

public class GoogleFilesChooserActivity extends Activity implements View.OnClickListener, IGoogleSearch {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.andreaak.cards.R.layout.activity_google_files_chooser);

        listView = (ListView) findViewById(com.andreaak.cards.R.id.lvMain);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttonOk = (Button) findViewById(com.andreaak.cards.R.id.buttonOk);
        buttonOk.setOnClickListener(this);
        buttonCancel = (Button) findViewById(com.andreaak.cards.R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        helper = GoogleDriveHelper.getInstance();
        fill();
    }

    private void fill() {
        final boolean[] isDownload = {false};
        final IGoogleSearch act = this;
        databaseFiles = new ArrayList();
        setTitle(com.andreaak.cards.R.string.search);

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    List<GoogleItem> directory = helper.search("root", Configs.GoogleDir, null);
                    if (directory != null && directory.size() == 1) {
                        ArrayList<GoogleItem> findFiles = helper.search(directory.get(0).getId(), null, null);
                        for (GoogleItem file : findFiles) {
                            if (!helper.isFolder(file) && file.getTitle().endsWith(Configs.LessonsExtension)) {
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
                    act.onSearchOk();
                } else {
                    act.onSearchFail(ex);
                }
            }
        }.execute();
    }

    @Override
    public void onSearchOk() {
        adapter = new GoogleArrayAdapter(this, com.andreaak.cards.R.layout.list_item_google_files_chooser, databaseFiles);
        listView.setAdapter(adapter);
        setTitle(com.andreaak.cards.R.string.app_name);
    }

    @Override
    public void onSearchFail(Exception ex) {
        Utils.showText(this, com.andreaak.cards.R.string.search_fault);
        setTitle(com.andreaak.cards.R.string.app_name);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case com.andreaak.cards.R.id.buttonOk:
                ProcessSelection();
                break;
            case com.andreaak.cards.R.id.buttonCancel:
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
        Intent intent1 = new Intent(this, DirectoryChooserActivity.class);
        startActivityForResult(intent1, REQUEST_DIRECTORY_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DIRECTORY_CHOOSER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooserActivity.PATH);
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