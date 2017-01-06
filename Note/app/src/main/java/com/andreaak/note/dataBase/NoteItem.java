package com.andreaak.note.dataBase;

import com.andreaak.note.utils.Item;
import com.andreaak.note.utils.ItemType;

public class NoteItem extends Item {
    private int id;
    private int parentId;
    private String description;
    private String path;

    public NoteItem(int id, int parentId, String description, String path, ItemType type) {
        super(type);
        this.id = id;
        this.parentId = parentId;
        this.description = description;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }
}
