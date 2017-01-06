package com.andreaak.note.dataBase;

import com.andreaak.note.utils.Item;
import com.andreaak.note.utils.ItemType;

public class NoteItem extends Item {
    private int id;

    public int getId() {
        return id;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public NoteItem(int id, String description, ItemType type) {
        super(type);
        this.id = id;
        this.description = description;
    }
}
