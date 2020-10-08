package com.andreaak.common.google;

import com.andreaak.common.fileSystemItems.FileSystemItem;
import com.andreaak.common.fileSystemItems.ItemType;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncItem extends FileSystemItem implements Serializable {

    private File file;
    private GoogleItem googleItem;
    private SyncItem parent;
    private List<SyncItem> items;

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
        if (googleItem != null) {
            isFolder = googleItem.isFolder();
        } else if (file.exists()) {
            isFolder = file.isDirectory();
        }
        return isFolder ? ItemType.Directory : ItemType.File;
    }

    public void init() {

        items = new ArrayList<>();


        ArrayList<GoogleItem> subGoogleItems = null;
        if (googleItem != null) {
            subGoogleItems = GoogleDriveHelper.getInstance().search(googleItem.getId(), null, null);
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                GoogleItem subGoogleItem = getGoogleItem(subGoogleItems, subFile.getName());
                SyncItem syncItem = new SyncItem(subFile, subGoogleItem, this);
                if (subFile.isDirectory()) {
                    syncItem.init();
                }
                items.add(syncItem);
                if (subGoogleItem != null) {
                    subGoogleItems.remove(subGoogleItem);
                }
            }
        }

        if (subGoogleItems != null) {
            for (GoogleItem googleItem : subGoogleItems) {
                File subFile = getFile(file.getAbsolutePath(), googleItem.getTitle());
                SyncItem syncItem = new SyncItem(subFile, googleItem, this);
                if (googleItem.isFolder()) {
                    syncItem.init();
                }
                items.add(syncItem);
            }
        }
    }

    public boolean synchronize() {
        boolean res = true;
        for (SyncItem item : items) {
            if (item.doesRemoteExist()) {
                if (item.isFolder()) {
                    res &= item.synchronize();
                } else if (item.isNeedDownload()) {
                    res &= GoogleDriveHelper.getInstance().saveToFile2(item.googleItem.getId(), item.file);
                }
            } else {
                delete(item.file);
            }
        }
        return res;
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    delete(subFile);
                }
            }
        }
        file.delete();
    }

    private GoogleItem getGoogleItem(ArrayList<GoogleItem> googleItems, String name) {
        if (googleItems == null || googleItems.isEmpty()) {
            return null;
        }

        for (GoogleItem item : googleItems) {
            if (name.equalsIgnoreCase(item.getTitle())) {
                return item;
            }
        }
        return null;
    }

    private File getFile(String path, String title) {

        return new File(path + "/" + title);
    }

    public boolean isNeedDownload() {
        if (!doesRemoteExist()) {
            return false;
        }
        return new Date(file.exists() ? file.lastModified() : 0).getTime() < googleItem.getDate().getTime();
    }

    public boolean isFolder() {
        if (doesRemoteExist()) {
            return googleItem.isFolder();
        }
        return file.isDirectory();
    }

    private boolean doesRemoteExist() {
        return googleItem != null;
    }
}
