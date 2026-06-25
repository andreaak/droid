package com.andreaak.common.utils;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DriveRepository {

    private final Drive driveService;

    public DriveRepository(Drive driveService) {
        this.driveService = driveService;
    }

    // =========================================================
    // Поиск папки Deutsch
    // =========================================================

    public String getGoogleDriveFolderId(String path) throws Exception {

        String[] items = path.split("/");

        String parentId = null;

        for (String item : items) {
            String folderdId = findFolderId(parentId, item);

            if (folderdId == null) {
                throw new Exception("Folder not found " + item);
            }

            parentId = folderdId;
        }

        return parentId;
    }

    private String findFolderId(String parentId, String folderName) throws Exception {

        String query;

        if (parentId == null) {
            query =
                    "mimeType='application/vnd.google-apps.folder' " +
                            "and name='" + folderName + "' " +
                            "and trashed=false";
        } else {
            query =
                    "'" + parentId + "' in parents " +
                            "and mimeType='application/vnd.google-apps.folder' " +
                            "and name='" + folderName + "' " +
                            "and trashed=false";
        }

        FileList result =
                driveService.files()
                        .list()
                        .setQ(query)
                        .setFields("files(id,name)")
                        .execute();

        List<com.google.api.services.drive.model.File> files =
                result.getFiles();

        return (files == null || files.isEmpty())
                ? null
                : files.get(0).getId();
    }

    // =========================================================
    // Получение списка файлов из папки
    // =========================================================

    public List<com.google.api.services.drive.model.File>
        getFilesFromFolder(String folderId, String filter) throws Exception {
            List<com.google.api.services.drive.model.File> allFiles =
                    new ArrayList<>();
            String fl = String.format("and name contains '%s' ", filter);
            String pageToken = null;

            do {

                FileList result =
                        driveService.files()
                                .list()
                                .setQ(
                                        "'" + folderId + "' in parents " +
                                                fl +
                                                "and trashed=false"
                                )
                                .setFields(
                                        "nextPageToken, files(id,name,size,modifiedTime)"
                                )
                                .setPageSize(1000)
                                .setPageToken(pageToken)
                                .execute();

                allFiles.addAll(result.getFiles());

                pageToken = result.getNextPageToken();

            } while (pageToken != null);

            Collections.sort(
                    allFiles,
                    new Comparator<com.google.api.services.drive.model.File>() {
                        @Override
                        public int compare(
                                com.google.api.services.drive.model.File f1,
                                com.google.api.services.drive.model.File f2) {

                            long t1 = f1.getModifiedTime().getValue();
                            long t2 = f2.getModifiedTime().getValue();

                            return Long.compare(t2, t1);
                        }
                    }
            );

            return allFiles;
    }

    public Map<String, com.google.api.services.drive.model.File>
        getFiles(String folderId) throws Exception {

        Map<String, File> result = new TreeMap<String, File>();

        collectDriveFiles(folderId,"", result);

        return result;
    }

    private void collectDriveFiles(
            String folderId,
            String currentPath,
            Map<String,
                    com.google.api.services.drive.model.File> result)
            throws Exception {

        String pageToken = null;

        do {

            FileList fileList =
                    driveService.files()
                            .list()
                            .setQ(
                                    "'" + folderId + "' in parents " +
                                            "and trashed=false"
                            )
                            .setFields(
                                    "nextPageToken," +
                                            "files(id,name,mimeType,modifiedTime)"
                            )
                            .setPageSize(1000)
                            .setPageToken(pageToken)
                            .execute();

            for (com.google.api.services.drive.model.File file
                    : fileList.getFiles()) {

                if ("application/vnd.google-apps.folder"
                        .equals(file.getMimeType())) {

                    collectDriveFiles(
                            file.getId(),
                            currentPath
                                    + file.getName()
                                    + "/",
                            result
                    );

                } else {

                    result.put(
                            currentPath + file.getName(),
                            file
                    );
                }
            }

            pageToken = fileList.getNextPageToken();

        } while (pageToken != null);
    }


    // =========================================================
    // Скачивание файла
    // =========================================================

    public java.io.File downloadFileToFolder(
            com.google.api.services.drive.model.File driveFile,
            java.io.File destinationFolder
    ) throws Exception {

        java.io.File localFile =
                new java.io.File(
                        destinationFolder,
                        driveFile.getName()
                );

        downloadFile(driveFile, localFile);

        return localFile;
    }

    public java.io.File downloadFile(
            com.google.api.services.drive.model.File driveFile,
            java.io.File localFile
    ) throws Exception {

        InputStream inputStream =
                driveService.files()
                        .get(driveFile.getId())
                        .executeMediaAsInputStream();

        FileOutputStream outputStream =
                new FileOutputStream(localFile);

        byte[] buffer = new byte[4096];

        int length;

        while ((length = inputStream.read(buffer)) > 0) {

            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();

        inputStream.close();

        return localFile;
    }
}