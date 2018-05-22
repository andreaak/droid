package com.andreaak.note.dataBase;

import com.andreaak.note.fileSystemItems.FileSystemItem;
import com.andreaak.note.fileSystemItems.ItemType;

public class EntityItem extends FileSystemItem {
    private int id;
    private String description;

    public EntityItem(int id, String description, ItemType type) {
        super(type);
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}

