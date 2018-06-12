package com.andreaak.common.google;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.andreaak.common.R;
import com.andreaak.common.configs.Configs;

import java.io.File;
import java.util.Date;
import java.util.List;

public class GoogleArrayAdapter extends ArrayAdapter<GoogleItem> {

    private Context c;
    private int id;
    private List<GoogleItem> items;

    public GoogleArrayAdapter(Context context, int textViewResourceId,
                              List<GoogleItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public GoogleItem getItem(int i) {

        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }

        final GoogleItem item = items.get(position);
        if (item != null) {
            CheckedTextView tvName = (CheckedTextView) view.findViewById(R.id.checkedItem);
            if (tvName != null)
                tvName.setText(item.getTitle());
            TextView txDate = (TextView) view.findViewById(R.id.textViewDate);
            if (txDate != null) {
                String text = item.getFormattedDate();
                if (getModifiedDate(item.getTitle()).getTime() < item.getDate().getTime()) {
                    text += " (new)";
                }
                txDate.setText(text);
            }
        }
        return view;
    }

    private Date getModifiedDate(String fileName) {
        String filePath = Configs.getInstance().WorkingDir + "/" + fileName;
        File file = new File(filePath);
        return new Date(file.exists() ? file.lastModified() : 0);
    }
}
