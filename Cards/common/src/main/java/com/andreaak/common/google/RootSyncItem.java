package com.andreaak.common.google;

import java.io.File;
import java.io.Serializable;

public class RootSyncItem implements Serializable {
    public SyncItem item;
    public String rootFolderPath;
    public String remoteRootFolderPath;

    public RootSyncItem(String rootFolderPath, String remoteRootFolderPath) {
        this.rootFolderPath = rootFolderPath;
        this.remoteRootFolderPath = remoteRootFolderPath;
    }

    public void init() {
        GoogleItem googleItem = GoogleDriveHelper.getInstance().searchFolder("root", remoteRootFolderPath);
        File file = new File(rootFolderPath);

        item = new SyncItem(file, googleItem);
        item.init();
    }

    public boolean synchronize() {
        return item.synchronize();
    }
}
