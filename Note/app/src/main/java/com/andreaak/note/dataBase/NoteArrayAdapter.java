package com.andreaak.note.dataBase;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreaak.note.R;

import java.util.List;

public class NoteArrayAdapter extends ArrayAdapter<NoteItem> {

    private Context c;
    private int id;
    private List<NoteItem> items;

    public NoteArrayAdapter(Context context, int textViewResourceId,
                            List<NoteItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public NoteItem getItem(int i) {

        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }

        final NoteItem item = items.get(position);
        if (item != null) {
            TextView tvName = (TextView) view.findViewById(R.id.TextViewName);
            TextView tvData = (TextView) view.findViewById(R.id.TextViewData);
            TextView tvDate = (TextView) view.findViewById(R.id.TextViewDate);
            ImageView imageCity = (ImageView) view.findViewById(R.id.fd_Icon1);

            Drawable image = c.getResources().getDrawable(item.GetImageId());
            imageCity.setImageDrawable(image);

            if (tvName != null)
                tvName.setText(item.getDescription());
            if (tvData != null)
                tvData.setText("");
            if (tvDate != null)
                tvDate.setText("");
        }
        return view;
    }
}
