package com.andreaak.note.dataBase;

import com.andreaak.common.fileSystemItems.ItemType;

public class FindNoteItem extends EntityItem implements Comparable<FindNoteItem> {

    private String path;

    public FindNoteItem(int id, String description, ItemType type, String path) {
        super(id, description, type, 0);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int compareTo(FindNoteItem o) {
        if (this.getPath() != null)
            return this.getPath().toLowerCase().compareTo(o.getPath().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
