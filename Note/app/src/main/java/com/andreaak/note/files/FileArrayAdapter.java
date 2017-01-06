package com.andreaak.note.files;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreaak.note.R;
import com.andreaak.note.files.FileItem;

public class FileArrayAdapter extends ArrayAdapter<FileItem> {

    private Context c;
    private int id;
    private List<FileItem> items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<FileItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public FileItem getItem(int i) {

        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(id, null);
        }

               /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        final FileItem item = items.get(position);
        if (item != null) {
            TextView tvName = (TextView) view.findViewById(R.id.TextViewName);
            TextView tvData = (TextView) view.findViewById(R.id.TextViewData);
            TextView tvDate = (TextView) view.findViewById(R.id.TextViewDate);
            ImageView imageCity = (ImageView) view.findViewById(R.id.fd_Icon1);

            Drawable image = c.getResources().getDrawable(item.GetImageId());
            imageCity.setImageDrawable(image);

            if (tvName != null)
                tvName.setText(item.getName());
            if (tvData != null)
                tvData.setText(item.getData());
            if (tvDate != null)
                tvDate.setText(item.getDate());
        }
        return view;
    }
}
