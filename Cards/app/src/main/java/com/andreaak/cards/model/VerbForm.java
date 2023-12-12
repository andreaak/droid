package com.andreaak.cards.model;

import com.andreaak.cards.utils.XmlParser;
import com.andreaak.common.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VerbForm implements java.io.Serializable {

    private ArrayList<VerbFormItem> verbFormItems;
    private File file;
    private String displayName;

    public VerbForm(File file) {

        this.file = file;
        this.displayName = Utils.normalize(Utils.getDisplayName(file.getName(), ""));
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<VerbFormItem> getVerbFormItems() {
        if(verbFormItems == null) {
            XmlParser.parseVerbForm(this);
         }

        return verbFormItems;
    }

    public void add(VerbFormItem item) {
        verbFormItems.add(item);
    }

    public void clear() {
        if(verbFormItems == null) {
            verbFormItems = new ArrayList<>();
        } else {
            verbFormItems.clear();
        }
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
