package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.VerbForm;
import com.andreaak.common.utils.Utils;

public class VerbFormSpinAdapter extends ArrayAdapter<VerbForm> {

    private Context context;
    private VerbForm[] values;

    public VerbFormSpinAdapter(Context context, int textViewResourceId,
                               VerbForm[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public VerbForm getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(Utils.getFileNameWithoutExtensions(values[position].getDisplayName()));
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(Utils.getFileNameWithoutExtensions(values[position].getDisplayName()));
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }
}

