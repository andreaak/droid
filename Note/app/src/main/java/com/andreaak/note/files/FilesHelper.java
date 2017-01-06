package com.andreaak.note.files;

import android.content.Context;
import android.util.Log;

import com.andreaak.note.R;
import com.andreaak.note.Constants;
import com.andreaak.note.utils.ItemType;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilesHelper {

    private static final String ROOT_DIRECTORY = "";

    private final Context context;

    public FilesHelper(Context context) {
        this.context = context;
    }

    public List<FileItem> getDirectory(File parent) {
        File[] dirs = parent.listFiles();
        List<FileItem> directories = new ArrayList<FileItem>();
        List<FileItem> files = new ArrayList<FileItem>();
        try {
            for (File file : dirs) {
                Date lastModDate = new Date(file.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (file.isDirectory()) {
                    File[] filesInDirectory = file.listFiles();
                    int filesCount = filesInDirectory != null ? filesInDirectory.length : 0;

                    int id = filesCount == 0 ? R.string.item : R.string.items;
                    String num_item = Constants.getText(String.valueOf(filesCount), context.getString(id));
                    directories.add(new FileItem(file.getName(), num_item, date_modify, file.getAbsolutePath(), ItemType.Directory));
                } else {
                    float length = file.length() / 1000000f;
                    DecimalFormat df = new DecimalFormat("#.00");
                    files.add(new FileItem(file.getName(), df.format(length) + context.getString(R.string.bytes), date_modify, file.getAbsolutePath(), ItemType.File));
                }
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        Collections.sort(directories);
        Collections.sort(files);
        directories.addAll(files);
        if (!parent.getName().equalsIgnoreCase(ROOT_DIRECTORY)) {
            directories.add(0, new FileItem("", context.getString(R.string.parentDirectory), "", parent.getParent(), ItemType.ParentDirectory));
        }
        return directories;
    }
}
