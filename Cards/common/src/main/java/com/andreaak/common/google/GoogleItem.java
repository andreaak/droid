package com.andreaak.common.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GoogleItem {
    private String title;
    private String id;
    private String mime;
    private Date date;
    private SimpleDateFormat dateFormat;

    public GoogleItem(File file) {
        this.id = file.getId();
        this.title = file.getTitle();
        this.mime = file.getMimeType();
        this.date = new Date(file.getModifiedDate().getValue());
        dateFormat = new SimpleDateFormat ("yyyy.MM.dd 'at' hh:mm:ss");
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

    public Date getDate() { return date; }

    public String getFormattedDate() { return dateFormat.format(date); }
}

