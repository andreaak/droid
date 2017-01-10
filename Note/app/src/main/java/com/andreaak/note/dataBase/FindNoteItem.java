package com.andreaak.note.dataBase;

import com.andreaak.note.utils.ItemType;

public class FindNoteItem extends EntityItem {

    private String path;

    public String getPath() {
        return path;
    }

    public FindNoteItem(int id, String description, ItemType type, String path) {
        super(id, description, type);
        this.path = path;
    }
}
