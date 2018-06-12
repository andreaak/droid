package com.andreaak.common.google;

import com.andreaak.common.configs.Configs;
import com.google.api.services.drive.model.File;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GoogleItem {
    private String title;
    private String id;
    private String mime;
    private Date date;
    private SimpleDateFormat dateFormat;
    private boolean isNewItem;

    public GoogleItem(File file) {
        this.id = file.getId();
        this.title = file.getTitle();
        this.mime = file.getMimeType();
        this.date = new Date(file.getModifiedDate().getValue());
        dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' hh:mm:ss");
        isNewItem = getDiskFileModifiedDate(title).getTime() < date.getTime();
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

    public boolean isNew() {
        return isNewItem;
    }

    //public String getFormattedDate() { return dateFormat.format(date); }

    private Date getDiskFileModifiedDate(String fileName) {
        String filePath = Configs.getInstance().WorkingDir + "/" + fileName;
        java.io.File file = new java.io.File(filePath);
        return new Date(file.exists() ? file.lastModified() : 0);
    }
}

