package com.andreaak.note.files;

import android.content.Context;

import com.andreaak.note.R;
import com.andreaak.note.adapters.Constants;
import com.andreaak.note.adapters.Item;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilesHelper {

    private static final String ROOT_DIRECTORY = "";
    private static final String PARENT_DIRECTORY_NAME = " ";
    private static final String PARENT_DIRECTORY_DATA = "..";
    Context context;

    public FilesHelper(Context context) {
        this.context = context;
    }

    public List<Item> getDirectory(File f) {
        File[] dirs = f.listFiles();
        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (ff.isDirectory()) {
                    File[] files = ff.listFiles();
                    int filesCount = files != null ? files.length : 0;

                    int id = filesCount == 0 ? R.string.item : R.string.items;
                    String num_item = Constants.getText(String.valueOf(filesCount), context.getString(id));

                    //String formated = lastModDate.toString();
                    dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), R.drawable.directory_icon));
                } else {
                    float length = ff.length() / 1000000f;
                    DecimalFormat df = new DecimalFormat("#.00");
                    fls.add(new Item(ff.getName(), df.format(length) + context.getString(R.string.bytes), date_modify, ff.getAbsolutePath(), R.drawable.file_icon));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase(ROOT_DIRECTORY))
            dir.add(0, new Item(PARENT_DIRECTORY_NAME, PARENT_DIRECTORY_DATA, "", f.getParent(), R.drawable.directory_up));
        return dir;
    }
}
