package com.andreaak.cards.utils.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.utils.Utils;

import java.io.File;

public class LessonsSpinAdapter extends ArrayAdapter<File> {

    private Context context;
    private File[] values;

    public LessonsSpinAdapter(Context context, int textViewResourceId,
                              File[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public File getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(Utils.getFileNameWithoutExtensions(values[position].getName()));
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(Utils.getFileNameWithoutExtensions(values[position].getName()));
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }
}
