package com.andreaak.common.fileSystemItems;

import android.content.Context;

import com.andreaak.common.R;
import com.andreaak.common.predicates.DirectoryPredicate;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

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

    public List<FileItem> getDirectory(File current, DirectoryPredicate predicate) {
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
                    } else if (addFiles && predicate.isValid(file)) {
                        FileItem fileItem = getFileItem(file, dateModify);
                        files.add(fileItem);
                    }
                }
            } catch (Exception ex) {
                Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
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
        FileSizeView view = new FileSizeView(file.length());
        return new FileItem(file.getName(), view.toString(), dateModify, file.getAbsolutePath(), ItemType.File);
    }
}

class FileSizeView {
    private float value;
    private String suffix;
    private DecimalFormat df = new DecimalFormat("#.00");

    public String toString() {
        return df.format(value) + " " + suffix;
    }

    public FileSizeView(long length) {
        if (length < 1000) {
            value = length;
            suffix = "bytes";
        } else if (length < 1000000) {
            value = ((float) length) / 1000;
            suffix = "KB";
        } else {
            value = ((float) length) / 1000000;
            suffix = "MB";
        }
    }
}
