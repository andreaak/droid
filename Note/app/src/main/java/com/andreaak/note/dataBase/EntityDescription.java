package com.andreaak.note.dataBase;

public class EntityDescription {
    private int id;

    public int getId() {
        return id;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public EntityDescription(int id, String description) {
        this.id = id;
        this.description = description;
    }
}
