package com.andreaak.note;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.andreaak.note.adapters.Constants;
import com.andreaak.note.adapters.FileArrayAdapter;
import com.andreaak.note.adapters.Item;

public class FileChooser extends ListActivity {

    private static final String ROOT_DIRECTORY = "";
    private static final String PARENT_DIRECTORY_NAME = " ";
    private static final String PARENT_DIRECTORY_DATA = "..";
    public static final String FILE_NAME = "FileName";
    public static final String PATH = "Path";
    private File sdCard = Environment.getExternalStorageDirectory();
    private File currentDir;
    private FileArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = sdCard;//new File("/" + ROOT_DIRECTORY + "/");
        fill(currentDir);
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        this.setTitle(f.getAbsolutePath());
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
                    String num_item = Constants.getText(String.valueOf(filesCount), getString(id));

                    //String formated = lastModDate.toString();
                    dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), Constants.DIRECTORY_ICON));
                } else {
                    float length = ff.length() / 1000000f;
                    DecimalFormat df = new DecimalFormat("#.00");
                    fls.add(new Item(ff.getName(), df.format(length) + getString(R.string.bytes), date_modify, ff.getAbsolutePath(), Constants.FILE_ICON));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase(ROOT_DIRECTORY))
            dir.add(0, new Item(PARENT_DIRECTORY_NAME, PARENT_DIRECTORY_DATA, "", f.getParent(), Constants.DIRECTORY_UP));
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getImage().equalsIgnoreCase(Constants.DIRECTORY_ICON) || o.getImage().equalsIgnoreCase(Constants.DIRECTORY_UP)) {
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra(PATH, currentDir.toString());
        intent.putExtra(FILE_NAME, o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }
}
