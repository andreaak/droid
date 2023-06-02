package com.andreaak.cards.activities.helpers;

import android.content.Context;

import com.andreaak.cards.R;
import com.andreaak.common.fileSystemItems.ItemType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileHelper {

    private String currentPath;
    private String rootPath;
    private Context context;

    public FileHelper(Context context, String currentPath) {
        this.context = context;
        rootPath = this.currentPath = currentPath;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public List<FileItem> getEntities(String currentPath) {

        List items = new ArrayList<FileItem>();
        if (currentPath == null) {
            return items;
        }

        File file = new File(currentPath);
        if (!file.exists()) {
            return items;
        }

        if (!currentPath.equals(rootPath)) {
            String parentPath = file.getParent();
            FileItem item = new FileItem(parentPath, context.getString(R.string.parentDirectory), ItemType.ParentDirectory);
            items.add(0, item);
        }

        File[] files = file.listFiles();
        Arrays.sort(files, new SortByName());
        for (File subFile : files) {
            ItemType type = subFile.isDirectory() ? ItemType.Directory : ItemType.File;
            FileItem item = new FileItem(subFile.getAbsolutePath(), subFile.getName(), type);
            items.add(item);
        }

        this.currentPath = currentPath;
        return items;
    }

    public String getDescriptions(String currentPath) {
        File file = new File(currentPath);
        if (!file.exists()) {
            return "Not found";
        }
        return file.getName();
    }
}

class SortByName implements Comparator<File> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(File a, File b)
    {
        return a.getName().compareTo(b.getName());
    }
}
