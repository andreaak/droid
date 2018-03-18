package com.andreaak.cards.utils.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.utils.LanguageItem;

import java.util.List;

public class LangSpinAdapter extends ArrayAdapter<LanguageItem> {

    private Context context;
    private List<LanguageItem> values;

    public LangSpinAdapter(Context context, int textViewResourceId,
                           List<LanguageItem> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public LanguageItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        LanguageItem lang = values.get(position);
        String text = lang.getPrimaryLanguage() + " --> " + lang.getSecondaryLanguage();
        label.setText(text);
        label.setTextSize(20);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        LanguageItem lang = values.get(position);
        String text = lang.getPrimaryLanguage() + " --> " + lang.getSecondaryLanguage();
        label.setText(text);
        label.setTextSize(20);
        label.setPadding(2, 5, 2, 2);

        return label;
    }
}