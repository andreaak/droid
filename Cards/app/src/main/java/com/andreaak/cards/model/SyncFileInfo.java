package com.andreaak.cards.model;

public class SyncFileInfo {

    public String relativePath;

    public com.google.api.services.drive.model.File driveFile;

    @Override
    public String toString() {

        return relativePath;
    }
}