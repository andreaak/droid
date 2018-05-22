package com.andreaak.note.fileSystemItems;

import com.andreaak.note.R;

public abstract class FileSystemItem {

    private ItemType type;

    public FileSystemItem(ItemType type) {
        this.type = type;
    }

    public ItemType getType() {
        return type;
    }

    public int GetImageId() {
        switch (getType()) {
            case Directory:
                return R.drawable.directory_icon;
            case File:
                return R.drawable.file_icon;
            case ParentDirectory:
                return R.drawable.directory_up;
        }
        throw new IllegalArgumentException("Wrong item type");
    }
}
