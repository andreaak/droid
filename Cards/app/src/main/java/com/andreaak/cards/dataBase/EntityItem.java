package com.andreaak.cards.dataBase;

import com.andreaak.cards.utils.Item;
import com.andreaak.cards.utils.ItemType;

public class EntityItem extends Item {
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

