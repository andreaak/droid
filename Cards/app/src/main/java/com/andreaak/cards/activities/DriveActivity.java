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
import com.andreaak.common.utils.DriveRepository;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
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

    private final List<File>
            driveFiles = new ArrayList<>();

    private DriveRepository repository;

    private java.io.File destinationFolder;

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

                        updateDownloadButtonState();
                    }
                }
        );


        buttonDownload = findViewById(R.id.buttonDownload);

        buttonDownload.setOnClickListener(v -> {
                    buttonDownload.setEnabled(false);
                    downloadSelectedFiles();
                }
        );
        buttonDownload.setEnabled(false);

        buttonSelectAll =
                findViewById(R.id.buttonSelectAll);

        buttonSelectAll.setOnClickListener(
                v -> toggleSelection()
        );

        prefixes = getIntent().getStringExtra(PREFIXES);

        String directories = getIntent().getStringExtra(PATH);
        destinationFolder = new java.io.File(directories);

        signIn();
    }

    // =========================================================
    // Google Sign-In
    // =========================================================

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

                loadFilesInfo(AppConfigs.getInstance().GoogleDir);

            } catch (Exception e) {

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

                String folderId =
                        repository.getGoogleDriveFolderId(path);

                if (folderId == null) {
                    return;
                }

                List<File> files = repository.getFilesFromFolder(folderId, prefixes);
                List<File> filteredFiles = filterFiles(files, destinationFolder);

                driveFiles.clear();
                driveFiles.addAll(filteredFiles);

                fileNames.clear();

                for (File
                        file : filteredFiles) {

                    Date date = new Date(file.getModifiedTime().getValue());

                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd.MM.yyyy",
                            Locale.getDefault());

                    String text = sdf.format(date);

                    fileNames.add(file.getName().replace(".xml", "") + " -- " + text);
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {

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

        adapter.notifyDataSetChanged();
    }

    // =========================================================
    // Скачать выбранные файлы
    // =========================================================

    private void downloadSelectedFiles() {


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    final ArrayList<Integer> downloadedPositions = new ArrayList<>();

                    SparseBooleanArray checked = listView.getCheckedItemPositions();

                    for (int i = 0; i < checked.size(); i++) {

                        int position = checked.keyAt(i);

                        if (checked.valueAt(i)) {

                            File driveFile = driveFiles.get(position);

                            repository.downloadFile(driveFile, destinationFolder);

                            downloadedPositions.add(position);
                        }
                    }

                    loadFilesInfo(AppConfigs.getInstance().GoogleDir);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

//                            // снять выделение только у скачанных файлов
//                            for (Integer position : downloadedPositions) {
//
//                                listView.setItemChecked(position, false);
//                            }
//                            adapter.notifyDataSetChanged();
                            buttonDownload.setEnabled(false);
                            Toast.makeText(
                                    DriveActivity.this,
                                    "Download completed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateDownloadButtonState() {

        SparseBooleanArray checked =
                listView.getCheckedItemPositions();

        boolean hasSelected = false;

        for (int i = 0; i < checked.size(); i++) {

            if (checked.valueAt(i)) {

                hasSelected = true;
                break;
            }
        }

        buttonDownload.setEnabled(hasSelected);
    }
}