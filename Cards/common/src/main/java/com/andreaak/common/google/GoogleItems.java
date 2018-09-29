package com.andreaak.common.google;

import java.io.Serializable;
import java.util.List;

public class GoogleItems implements Serializable {

    private GoogleItem[] items;

    public GoogleItems(List<GoogleItem> selectedFiles) {
        items = selectedFiles.toArray(new GoogleItem[0]);
    }

    public GoogleItem[] getItems() {
        return items;
    }
}
