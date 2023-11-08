package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.VerbItem;

import java.util.List;

public class VerbSpinAdapter extends ArrayAdapter<VerbItem> {

    private Context context;
    private List<VerbItem> values;

    public VerbSpinAdapter(Context context, int textViewResourceId,
                           List<VerbItem> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public VerbItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = new TextView(context);
        label.setText(values.get(position)._1);
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values.get(position)._1);
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }
}
