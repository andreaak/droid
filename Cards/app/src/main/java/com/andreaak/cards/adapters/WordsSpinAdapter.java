package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.WordItem;

import java.util.List;

public class WordsSpinAdapter extends ArrayAdapter<WordItem> {

    private Context context;
    private List<WordItem> values;
    private String language;

    public WordsSpinAdapter(Context context, int textViewResourceId,
                            List<WordItem> values, String language) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
        this.language = language;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public WordItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = new TextView(context);
        label.setText(values.get(position).getValue(language));
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values.get(position).getValue(language));
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }
}
