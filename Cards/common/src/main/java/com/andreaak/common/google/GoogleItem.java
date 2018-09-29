package com.andreaak.common.google;

import com.google.api.services.drive.model.File;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GoogleItem implements Serializable {
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

    public boolean isFolder() {
        return mime != null && GoogleDriveHelper.MIME_FLDR.equalsIgnoreCase(mime);
    }

    public void setIsNew(String directoryPath) {
        String filePath = directoryPath + "/" + title;
        java.io.File file = new java.io.File(filePath);
        isNewItem = new Date(file.exists() ? file.lastModified() : 0).getTime() < date.getTime();
    }

    //public String getFormattedDate() { return dateFormat.format(date); }

//    private Date getDiskFileModifiedDate(String fileName) {
//        String filePath = Configs.getInstance().WorkingDir + "/" + fileName;
//        java.io.File file = new java.io.File(filePath);
//        return new Date(file.exists() ? file.lastModified() : 0);
//    }
}

