package com.andreaak.cards.utils;

import com.andreaak.cards.R;

public abstract class Item {

    private ItemType type;

    public Item(ItemType type) {
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
