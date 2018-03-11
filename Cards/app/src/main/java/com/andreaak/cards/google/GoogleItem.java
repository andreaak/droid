package com.andreaak.cards.google;

import com.google.api.services.drive.model.File;

public class GoogleItem {
    private String title;
    private String id;
    private String mime;
    private String date;

    public GoogleItem(File file) {
        this.id = file.getId();
        this.title = file.getTitle();
        this.mime = file.getMimeType();
        this.date = file.getModifiedDate() != null ? file.getModifiedDate().toString() : "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMime() {
        return mime;
    }

    public String getModifiedDate() {
        return date;
    }
}

