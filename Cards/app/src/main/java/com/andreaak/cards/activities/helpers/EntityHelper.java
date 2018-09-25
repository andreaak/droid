package com.andreaak.cards.activities.helpers;

import android.content.Context;

import com.andreaak.cards.R;
import com.andreaak.common.fileSystemItems.ItemType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntityHelper {

    private String currentPath;
    private String rootPath;
    private Context context;

    public EntityHelper(Context context, String currentPath) {
        this.context = context;
        rootPath = this.currentPath = currentPath;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public List<EntityItem> getEntities(String currentPath) {

        List items = new ArrayList<EntityItem>();
        if (currentPath == null) {
            return items;
        }

        File file = new File(currentPath);
        if (!file.exists()) {
            return items;
        }

        if (!currentPath.equals(rootPath)) {
            String parentPath = file.getParent();
            EntityItem item = new EntityItem(parentPath, context.getString(R.string.parentDirectory), ItemType.ParentDirectory);
            items.add(0, item);
        }
        for (File subFile : file.listFiles()) {
            ItemType type = subFile.isDirectory() ? ItemType.Directory : ItemType.File;
            EntityItem item = new EntityItem(subFile.getAbsolutePath(), subFile.getName(), type);
            items.add(item);
        }

        this.currentPath = currentPath;
        return items;
    }

    public String getDescriptions(String currentPath) {
        File file = new File(currentPath);
        if (!file.exists()) {
            return "Not found";
        }
        return file.getName();
    }
}
