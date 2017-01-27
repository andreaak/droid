package com.andreaak.note.files;

import android.content.Context;

import com.andreaak.note.R;
import com.andreaak.note.utils.Configs;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.ItemType;
import com.andreaak.note.utils.Utils;
import com.andreaak.note.utils.logger.Logger;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilesHelper {

    private static final String ROOT_DIRECTORY = "";

    private final Context context;
    private final boolean addFiles;
    private FileItem currentDirectory;

    public FilesHelper(Context context, boolean addFiles) {
        this.context = context;
        this.addFiles = addFiles;
    }

    public FileItem getCurrentDirectory() {
        return currentDirectory;
    }

    public List<FileItem> getDirectory(File current) {
        List<FileItem> directories = new ArrayList<FileItem>();
        List<FileItem> files = new ArrayList<FileItem>();

        String dateModify = getDateModify(current);
        currentDirectory = getDirectoryItem(current, dateModify);

        File[] dirs = current.listFiles();
        if (dirs != null) {
            try {
                for (File file : dirs) {
                    dateModify = getDateModify(file);

                    if (file.isDirectory()) {
                        FileItem fileItem = getDirectoryItem(file, dateModify);
                        directories.add(fileItem);
                    } else if (addFiles && file.getName().endsWith(Configs.DatabaseExtension)) {
                        FileItem fileItem = getFileItem(file, dateModify);
                        files.add(fileItem);
                    }
                }
            } catch (Exception e) {
                Logger.e(Constants.LOG_TAG, e.getMessage(), e);
            }
        }

        Collections.sort(directories);
        Collections.sort(files);
        directories.addAll(files);
        if (!current.getName().equalsIgnoreCase(ROOT_DIRECTORY)) {
            directories.add(0, new FileItem("", context.getString(R.string.parentDirectory), "", current.getParent(), ItemType.ParentDirectory));
        }
        return directories;
    }

    private String getDateModify(File file) {
        Date lastModDate = new Date(file.lastModified());
        DateFormat formatter = DateFormat.getDateTimeInstance();
        return formatter.format(lastModDate);
    }

    private FileItem getDirectoryItem(File file, String dateModify) {
        File[] filesInDirectory = file.listFiles();
        int filesCount = filesInDirectory != null ? filesInDirectory.length : 0;

        int id = filesCount == 0 ? R.string.item : R.string.items;
        String[] args = new String[]{String.valueOf(filesCount), context.getString(id)};
        String num_item = Utils.getSeparatedText(" ", Arrays.asList(args));
        return new FileItem(file.getName(), num_item, dateModify, file.getAbsolutePath(), ItemType.Directory);
    }

    private FileItem getFileItem(File file, String dateModify) {
        float length = file.length() / 1000000f;
        DecimalFormat df = new DecimalFormat("#.00");
        return new FileItem(file.getName(), df.format(length) + context.getString(R.string.bytes), dateModify, file.getAbsolutePath(), ItemType.File);
    }
}
