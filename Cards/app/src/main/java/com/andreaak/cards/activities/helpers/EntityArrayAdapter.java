package com.andreaak.cards.activities.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreaak.cards.R;

import java.util.List;

public class EntityArrayAdapter extends ArrayAdapter<EntityItem> {

    private Context c;
    private int id;
    private List<EntityItem> items;

    public EntityArrayAdapter(Context context, int textViewResourceId,
                              List<EntityItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public EntityItem getItem(int i) {

        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }

        final EntityItem item = items.get(position);
        if (item != null) {
            TextView tvName = (TextView) view.findViewById(R.id.TextViewName);
            ImageView imageView = (ImageView) view.findViewById(R.id.fd_Icon1);

            Drawable image = c.getResources().getDrawable(item.GetImageId());
            imageView.setImageDrawable(image);

            if (tvName != null)
                tvName.setText(item.getDescription());
        }
        return view;
    }
}
