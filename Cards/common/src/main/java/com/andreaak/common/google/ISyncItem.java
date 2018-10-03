package com.andreaak.common.google;

import java.util.List;

public interface ISyncItem {
    void init();
    List<SyncItem> getItems();
}
