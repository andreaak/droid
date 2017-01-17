package com.andreaak.note.files;

import android.content.Context;
import android.util.Log;

import com.andreaak.note.R;
import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.ItemType;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DirectoriesHelper {

    private static final String ROOT_DIRECTORY = "";

    private final Context context;

    public DirectoriesHelper(Context context) {
        this.context = context;
    }

    public List<FileItem> getDirectory(File parent) {
        File[] dirs = parent.listFiles();
        List<FileItem> directories = new ArrayList<FileItem>();
        try {
            for (File file : dirs) {
                Date lastModDate = new Date(file.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (file.isDirectory()) {
                    File[] filesInDirectory = file.listFiles();
                    int filesCount = filesInDirectory != null ? filesInDirectory.length : 0;

                    int id = filesCount == 0 ? R.string.item : R.string.items;
                    String[] args = new String[]{String.valueOf(filesCount), context.getString(id)};
                    String num_item = Constants.getText(" ", Arrays.asList(args));
                    directories.add(new FileItem(file.getName(), num_item, date_modify, file.getAbsolutePath(), ItemType.Directory));
                }
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        Collections.sort(directories);
        if (!parent.getName().equalsIgnoreCase(ROOT_DIRECTORY)) {
            directories.add(0, new FileItem("", context.getString(R.string.parentDirectory), "", parent.getParent(), ItemType.ParentDirectory));
        }
        return directories;
    }
}
