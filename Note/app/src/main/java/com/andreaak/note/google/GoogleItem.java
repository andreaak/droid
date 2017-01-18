package com.andreaak.note.google;

import com.google.api.services.drive.model.File;

public class GoogleItem {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String mime;

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public GoogleItem(File file) {
        this.id = file.getId();
        this.title = file.getTitle();
        this.mime = file.getMimeType();
    }

    public GoogleItem(String id, String title, String mime) {
        this.id = id;
        this.title = title;
        this.mime = mime;
    }
}

