package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.VerbFormItem;

import java.util.List;

public class VerbTypesSpinAdapter extends ArrayAdapter<VerbFormItem> {

    private Context context;
    private List<VerbFormItem> values;

    public VerbTypesSpinAdapter(Context context, int textViewResourceId,
                           List<VerbFormItem> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public VerbFormItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        VerbFormItem item = values.get(position);
        String text = item.FormType.toString();
        label.setText(text);
        label.setTextSize(20);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        VerbFormItem item = values.get(position);
        String text = item.FormType.toString();
        label.setText(text);
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }
}
