package com.andreaak.note.dataBase;

import com.andreaak.common.fileSystemItems.FileSystemItem;
import com.andreaak.common.fileSystemItems.ItemType;

public class EntityItem extends FileSystemItem {
    private int id;
    private String description;
    private int position;
    private int parentId;

    public EntityItem(int id, String description, ItemType type, int position) {
        super(type);
        this.id = id;
        this.description = description;
        this.position = position;
    }

    public EntityItem(int id, String description, ItemType type, int position, int parentId) {
        this(id, description, type, position);
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }

    public int getParentId() {
        return parentId;
    }
}

