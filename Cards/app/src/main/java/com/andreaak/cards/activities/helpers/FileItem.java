package com.andreaak.cards.activities.helpers;

import com.andreaak.common.fileSystemItems.FileSystemItem;
import com.andreaak.common.fileSystemItems.ItemType;

public class FileItem extends FileSystemItem {
    private String path;
    private String description;

    public FileItem(String path, String description, ItemType type) {
        super(type);
        this.path = path;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }
}

