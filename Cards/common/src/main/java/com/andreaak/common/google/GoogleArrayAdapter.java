package com.andreaak.common.google;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.andreaak.common.R;

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
        }
        return view;
    }
}
