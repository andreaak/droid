package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.andreaak.cards.R;
import com.andreaak.cards.model.SyncFileInfo;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.DriveRepository;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.utils.logger.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.SparseBooleanArray;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DriveSyncActivity extends HandleExceptionActivity {

    public static final String LOCAL_PATH = "path";
    public static final String DRIVE_PATH = "drivepath";
    private static final int RC_SIGN_IN = 1001;

    private ListView listView;

    private Button buttonDownload;
    private Button buttonSelectAll;

    private ArrayAdapter<String> adapter;

    private final ArrayList<String> fileNames = new ArrayList<>();

    private final ArrayList<SyncFileInfo> syncFiles = new ArrayList<>();

    private DriveRepository repository;

    private java.io.File localRoot;
    private String driveRoot;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView( R.layout.activity_google_drive );

        listView = findViewById(R.id.listView);



        adapter = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_multiple_choice,
                        fileNames
                );

        listView.setAdapter(adapter);

        listView.setChoiceMode(
                ListView.CHOICE_MODE_MULTIPLE
        );

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateDownloadButtonState();
                    }
                }
        );

        buttonDownload = findViewById(R.id.buttonDownload);
        buttonDownload.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonDownload.setEnabled(false);
                        downloadSelectedFiles();
                    }
                }
        );
        buttonDownload.setEnabled(false);

        buttonSelectAll = findViewById(R.id.buttonSelectAll);

        buttonSelectAll.setOnClickListener(
                v -> toggleSelection()
        );

        String directory = getIntent().getStringExtra(LOCAL_PATH);
        localRoot = new java.io.File(directory);
        driveRoot = getIntent().getStringExtra(DRIVE_PATH);

        signIn();
    }

    private void signIn() {

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                        .requestEmail()
                        .requestScopes(
                                new Scope(DriveScopes.DRIVE_READONLY)
                        )
                        .build();

        GoogleSignInClient client =
                GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(
                client.getSignInIntent(),
                RC_SIGN_IN
        );


    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == RC_SIGN_IN) {

            try {

                GoogleSignInAccount account =
                        GoogleSignIn
                                .getSignedInAccountFromIntent(data)
                                .getResult();

                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                this,
                                Collections.singleton(
                                        DriveScopes.DRIVE_READONLY
                                )
                        );

                credential.setSelectedAccount(
                        account.getAccount()
                );

                Drive driveService =
                        new Drive.Builder(
                                new NetHttpTransport(),
                                GsonFactory.getDefaultInstance(),
                                credential
                        )
                                .setApplicationName("Cards")
                                .build();

                repository = new DriveRepository(driveService);

                loadFilesInfo();

            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(
                                        DriveSyncActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                                setTitle("Error");
                            }
                        }
                );
                e.printStackTrace();
            }
        }
    }

    private void loadFilesInfo() {
        setTitle("Loading info...");

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {

                            final List<SyncFileInfo> changedFiles = getChangedFiles();

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            syncFiles.clear();
                                            syncFiles.addAll(changedFiles);

                                            fileNames.clear();

                                            for (SyncFileInfo info : changedFiles) {

                                                fileNames.add( info.relativePath );
                                            }

                                            adapter.notifyDataSetChanged();

                                            if(fileNames.size() == 0) {
                                                setTitle("Not found");
                                                Toast.makeText(
                                                        DriveSyncActivity.this,
                                                        "Not found",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            } else {
                                                setTitle("Found " + fileNames.size());
                                            }
                                        }
                                    }
                            );

                        } catch (final Exception e) {
                            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            Toast.makeText(
                                                    DriveSyncActivity.this,
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            setTitle("Error");
                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }

    private List<SyncFileInfo> getChangedFiles()
            throws Exception {

        String folderId = repository.getGoogleDriveFolderId(driveRoot);
        Map<String,File> driveFiles = repository.getFiles(folderId);

        Map<String, java.io.File> localFiles = getLocalFiles(localRoot);

        List<SyncFileInfo> result = new ArrayList<>();

        for (String relativePath : driveFiles.keySet()) {

            File driveFile = driveFiles.get(relativePath);

            java.io.File localFile = localFiles.get(relativePath);

            boolean needDownload = false;

            if (localFile == null) {

                needDownload = true;

            } else {

                long driveTime = driveFile.getModifiedTime().getValue();
                if (driveTime > localFile.lastModified()) {
                    needDownload = true;
                }
            }

            if (needDownload) {

                SyncFileInfo info = new SyncFileInfo();
                info.relativePath =  relativePath;
                info.driveFile = driveFile;
                result.add(info);
            }
        }

        return result;
    }

    private Map<String, java.io.File> getLocalFiles(
            java.io.File rootFolder) {

        Map<String, java.io.File> result = new HashMap<>();

        if (!rootFolder.exists()) {
            return result;
        }

        collectLocalFiles(rootFolder, rootFolder, result);

        return result;
    }

    private void collectLocalFiles(
            java.io.File root,
            java.io.File current,
            Map<String, java.io.File> result) {

        java.io.File[] files = current.listFiles();

        if (files == null) {

            return;
        }

        for (java.io.File file : files) {

            if (file.isDirectory()) {

                collectLocalFiles(root, file, result);

            } else {
                String relativePath = root.toURI() .relativize(file.toURI()).getPath();
                result.put(relativePath, file);
            }
        }
    }

    private void downloadSelectedFiles() {

        buttonDownload.setEnabled(false);
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        final int count = checked.size();
        setTitle("Downloading files " + count);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            int cnt = count;
                            SparseBooleanArray checked = listView.getCheckedItemPositions();

                            final ArrayList<Integer> downloaded = new ArrayList<Integer>();

                            for (int i = 0; i < checked.size(); i++) {

                                int position = checked.keyAt(i);

                                if (!checked.valueAt(i)) {

                                    continue;
                                }

                                SyncFileInfo info = syncFiles.get(position);

                                java.io.File target = new java.io.File(localRoot, info.relativePath);

                                java.io.File parent = target.getParentFile();

                                if (!parent.exists()) {

                                    parent.mkdirs();
                                }

                                repository.downloadFile(info.driveFile, target);

                                //target.setLastModified( info.driveFile.getModifiedTime().getValue() );

                                downloaded.add(position);
                                int finalCnt = --cnt;
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                setTitle("Downloading files " + finalCnt);
                                            }
                                        }
                                );
                            }

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            for (Integer position : downloaded) {

                                                listView.setItemChecked(position, false);
                                            }

                                            updateDownloadButtonState();

                                            Toast.makeText(
                                                    DriveSyncActivity.this,
                                                    "Download completed",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            setTitle("Download completed");
                                            //loadFilesInfo();
                                        }
                                    }
                            );

                        } catch (final Exception e) {
                            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            buttonDownload.setEnabled(true);

                                            Toast.makeText(
                                                    DriveSyncActivity.this,
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            setTitle("Error");

                                        }
                                    }
                            );
                        }
                    }
                }
        ).start();
    }

    private void toggleSelection() {

        boolean allSelected = true;

        for (int i = 0; i < adapter.getCount(); i++) {

            if (!listView.isItemChecked(i)) {

                allSelected = false;
                break;
            }
        }

        if (allSelected) {

            // снять выделение
            listView.clearChoices();

            buttonSelectAll.setText("Select all");

        } else {

            // выделить все
            for (int i = 0; i < adapter.getCount(); i++) {
                listView.setItemChecked(i, true);
            }

            buttonSelectAll.setText("Unselect all");
        }
        updateDownloadButtonState();
        adapter.notifyDataSetChanged();
    }

    private void updateDownloadButtonState() {

        SparseBooleanArray checked = listView.getCheckedItemPositions();
        boolean hasSelection = checked.size() > 0;
        buttonDownload.setEnabled(hasSelection);
    }
}

