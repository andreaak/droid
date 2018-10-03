package com.andreaak.common.google;

import com.andreaak.common.fileSystemItems.FileSystemItem;
import com.andreaak.common.fileSystemItems.ItemType;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncItem extends FileSystemItem implements ISyncItem, Serializable {

    private File file;
    private GoogleItem googleItem;
    private SyncItem parent;
    private List<SyncItem> items;
    private boolean selected;

    public SyncItem(File file, GoogleItem googleItem, SyncItem parent) {
        super(getItemType(file, googleItem));
        this.file = file;
        this.googleItem = googleItem;
        this.parent = parent;
    }

    public SyncItem(File file, GoogleItem googleItem) {
        super(ItemType.RootDirectory);
        this.file = file;
        this.googleItem = googleItem;
    }

    private static ItemType getItemType(File file, GoogleItem googleItem) {

        boolean isFolder = true;
        if(googleItem != null) {
            isFolder = googleItem.isFolder();
        } else if(file.exists()) {
            isFolder = file.isDirectory();
        }
        return isFolder ? ItemType.Directory : ItemType.File;
    }

    public void init() {

        items = new ArrayList<>();
        File[] files = file.listFiles();

        ArrayList<GoogleItem> subGoogleItems = null;
        if(googleItem != null) {
            subGoogleItems = GoogleDriveHelper.getInstance().search(googleItem.getId(), null, null);
        }

        for(File subFile : files) {
            GoogleItem subGoogleItem = getGoogleItem(subGoogleItems, subFile.getName());
            SyncItem syncItem = new SyncItem(subFile, subGoogleItem, this);
            if(subFile.isDirectory()) {
                syncItem.init();
            }
            items.add(syncItem);
            if(subGoogleItem != null) {
                subGoogleItems.remove(subGoogleItem);
            }
        }

        for(GoogleItem googleItem : subGoogleItems) {
            File subFile = getFile(file.getAbsolutePath(), googleItem.getTitle());
            SyncItem syncItem = new SyncItem(subFile, googleItem, this);
            if(googleItem.isFolder()) {
                syncItem.init();
            }
            items.add(syncItem);
        }
    }

    public void download() {
        for(SyncItem item : items) {
            if(item.isFolder()) {
                item.download();
            } else if(item.isNeedDownload()) {
                GoogleDriveHelper.getInstance().saveToFile(item.googleItem.getId(), item.file);
            }
        }
    }

    private GoogleItem getGoogleItem(ArrayList<GoogleItem> googleItems, String name) {
        if(googleItems == null || googleItems.isEmpty()) {
            return null;
        }

        for(GoogleItem item : googleItems) {
            if(name.equalsIgnoreCase(item.getTitle())) {
                return item;
            }
        }
        return null;
    }

    private File getFile(String path, String title) {

        return new File(path + "/" + title);
    }

    private File getFile(File[] files, String title) {

        if(files == null || files.length == 0) {
            return null;
        }

        for(File file : files) {
            if(title.equalsIgnoreCase(file.getName())) {
                return file;
            }
        }
        return null;
    }

    public GoogleItem getGoogleItem() {
        return googleItem;
    }

    public List<SyncItem> getItems() {
        return items;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isNew() {
        if(!doesRemoteExist()) {
            return false;
        }
        return new Date(file.exists() ? file.lastModified() : 0).getTime() < googleItem.getDate().getTime();
    }

    public boolean isNeedDownload() {
        if(!doesRemoteExist()) {
            return false;
        }
        return new Date(file.exists() ? file.lastModified() : 0).getTime() < googleItem.getDate().getTime();
    }

    public boolean isFolder() {
        if(doesRemoteExist()) {
            return googleItem.isFolder();
        }
        return file.isDirectory();
    }

    private boolean doesRemoteExist() {
        return googleItem != null;
    }
}
