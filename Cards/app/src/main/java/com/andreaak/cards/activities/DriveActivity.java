package com.andreaak.cards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.cards.utils.Cache;
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

//import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.SparseBooleanArray;
import android.widget.Toast;

public class DriveActivity extends HandleExceptionActivity {

    private static final int RC_SIGN_IN = 1001;

    public static final String PATH = "path";
    public static final String PREFIXES = "prefixes";

    private ListView listView;

    private Button buttonSelectAll;
    private Button buttonDownload;

    private ArrayAdapter<String> adapter;

    private final List<String> fileNames = new ArrayList<>();

    private final List<File> driveFiles = new ArrayList<>();

    private DriveRepository repository;

    private java.io.File destinationFolder;

    private String googleDriveFolder;

    private String prefixes;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_google_drive);
        listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                fileNames
        );

        listView.setChoiceMode(
                ListView.CHOICE_MODE_MULTIPLE
        );

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateButtonState();
                    }
                }
        );


        buttonDownload = findViewById(R.id.buttonDownload);

        buttonDownload.setOnClickListener(v -> {

                    downloadSelectedFiles();
                }
        );

        buttonSelectAll =
                findViewById(R.id.buttonSelectAll);

        buttonSelectAll.setOnClickListener(
                v -> toggleSelection()
        );

        enableButtons(false);

        prefixes = getIntent().getStringExtra(PREFIXES);

        String directories = getIntent().getStringExtra(PATH);
        destinationFolder = new java.io.File(directories);
        googleDriveFolder = AppConfigs.getInstance().getRemoteLessonsDir();
        checkSignIn();
    }

    // =========================================================
    // Google Sign-In
    // =========================================================

    private void checkSignIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Scope driveScope = new Scope(DriveScopes.DRIVE_READONLY);

        if (account != null && GoogleSignIn.hasPermissions(account, driveScope)) {
            initDriveService(account);
        } else {
            signIn();
        }
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

    private void initDriveService(GoogleSignInAccount account) {
        try {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this,
                            Collections.singleton(DriveScopes.DRIVE_READONLY)
                    );

            credential.setSelectedAccount(account.getAccount());

            Drive driveService =
                    new Drive.Builder(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            credential
                    )
                            .setApplicationName("Cards")
                            .build();

            repository = new DriveRepository(driveService);

            setTitle("Loading info...");
            loadFilesInfo(googleDriveFolder);

        } catch (Exception e) {
            Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
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

                initDriveService(account);

            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    // =========================================================
    // Загрузка списка файлов
    // =========================================================

    private void loadFilesInfo(String path) {

        new Thread(() -> {

            try {

                String folderId = repository.getGoogleDriveFolderId(path);

                if (folderId == null) {
                    return;
                }

                List<File> files = repository.getFilesFromFolder(folderId, prefixes);
                List<File> filteredFiles = filterFiles(files, destinationFolder);

                driveFiles.clear();
                driveFiles.addAll(filteredFiles);

                fileNames.clear();

                for (File file : filteredFiles) {

                    Date date = new Date(file.getModifiedTime().getValue());

                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd.MM.yyyy",
                            Locale.getDefault());

                    String text = sdf.format(date);

                    fileNames.add(file.getName().replace(".xml", "") + " -- " + text);
                }

                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                updateButtonState();
                                adapter.notifyDataSetChanged();
                                if(fileNames.size() == 0) {
                                    setTitle("Not found");
                                    Toast.makeText(
                                            DriveActivity.this,
                                            "Not found",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    setTitle("Found " + fileNames.size());
                                }
                            }
                        }
                );

            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {

                                updateButtonState();

                                Toast.makeText(
                                        DriveActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                                setTitle("Error");

                            }
                        }
                );

                e.printStackTrace();
            }

        }).start();
    }

    private List<File> filterFiles(
            List<File> driveFiles,
            java.io.File localFolder) {

        List<File> result = new ArrayList<>();

        for (File driveFile : driveFiles) {

            java.io.File localFile = new java.io.File(localFolder, driveFile.getName());

            if (!localFile.exists()) {
                result.add(driveFile);
                continue;
            }

            long driveTime = driveFile.getModifiedTime().getValue();

            long localTime = localFile.lastModified();

            if (driveTime > localTime) {
                result.add(driveFile);
            }
        }

        return result;
    }

    // =========================================================
    // Скачать выбранные файлы
    // =========================================================

    private void downloadSelectedFiles() {
        enableButtons(false);

        SparseBooleanArray checked = listView.getCheckedItemPositions();
        if (checked == null) {
            return;
        }

        int selectedCount = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                selectedCount++;
            }
        }
        final int totalToDownload = selectedCount;
        setTitle("Downloading files " + totalToDownload);
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    final ArrayList<Integer> downloadedPositions = new ArrayList<>();

                    int cnt = totalToDownload;
                    for (int i = 0; i < checked.size(); i++) {

                        int position = checked.keyAt(i);

                        if (checked.valueAt(i)) {

                            File driveFile = driveFiles.get(position);

                            repository.downloadFileToFolder(driveFile, destinationFolder);

                            downloadedPositions.add(position);

                            int finalCnt = --cnt;
                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            setTitle("Downloading files " + finalCnt);
                                        }
                                    }
                            );                        }
                    }

                    Cache.getInstance().clear();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(
                                    DriveActivity.this,
                                    "Download completed",
                                    Toast.LENGTH_SHORT
                            ).show();
                            setTitle("Download completed");
                            loadFilesInfo(googleDriveFolder);
                        }
                    });

                } catch (Exception e) {
                    Logger.e(Constants.LOG_TAG, e.getMessage(), e);
                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {

                                    updateButtonState();

                                    Toast.makeText(
                                            DriveActivity.this,
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
        }).start();
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
        boolean enable = false;

        if (adapter != null && adapter.getCount() > 0) {
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            if (checked != null) {
                for (int i = 0; i < checked.size(); i++) {
                    if (checked.valueAt(i)) {
                        enable = true;
                        break;
                    }
                }
            }
        }

        buttonDownload.setEnabled(enable);
    }

    private void updateButtonState() {
        updateSelectAllButtonState();
        updateDownloadButtonState();
    }

    private void updateSelectAllButtonState() {
        int count = adapter != null ? adapter.getCount() : 0;
        if (count == 0) {
            buttonSelectAll.setEnabled(false);
            buttonSelectAll.setText("Select all");
            return;
        }

        buttonSelectAll.setEnabled(true);

        boolean allSelected = true;
        for (int i = 0; i < count; i++) {
            if (!listView.isItemChecked(i)) {
                allSelected = false;
                break;
            }
        }

        if (allSelected) {
            buttonSelectAll.setText("Unselect all");
        } else {
            buttonSelectAll.setText("Select all");
        }
    }

    private void enableButtons(boolean enable) {
        buttonDownload.setEnabled(enable);
        buttonSelectAll.setEnabled(enable);
    }
}