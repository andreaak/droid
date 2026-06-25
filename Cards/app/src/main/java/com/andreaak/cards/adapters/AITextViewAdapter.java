package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class AITextViewAdapter extends ArrayAdapter<String> {

    private final Object lock =  new Object();

    private ArrayList<String> items;
    private ArrayList<String> itemsAll;
    private ArrayList<String> suggestions;
    private int viewResourceId;

    @SuppressWarnings("unchecked")
    public AITextViewAdapter(Context context, int viewResourceId,
                             ArrayList<String> items) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<String>) items.clone();
        this.suggestions = new ArrayList<>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        TextView view;

        if (convertView == null) {

            view = (TextView) LayoutInflater.from(getContext())
                    .inflate(viewResourceId, parent, false);

        } else {

            view = (TextView) convertView;
        }

        view.setText(getItem(position));

        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            String str = resultValue.toString();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            synchronized (lock) {

                if (constraint != null) {
                    suggestions.clear();
                    String ct = Utils.normalizeForComparatorAndRemoveArtikles(constraint.toString());
                    for (String item : itemsAll) {
                        if (Utils.normalizeForComparatorAndRemoveArtikles(item)
                                .contains(ct)) {
                            suggestions.add(item);
                            if(suggestions.size() >= 100) {
                                break;
                            }
                        }
                    }

                } else {
                    suggestions.clear();
                    for (String item : itemsAll) {
                        suggestions.add(item);
                        if(suggestions.size() >= 100) {
                            break;
                        }
                    }
                }

                Collections.sort(suggestions, (a, b) -> Utils.normalizeForComparatorAndRemoveArtikles(a)
                        .compareTo(Utils.normalizeForComparatorAndRemoveArtikles(b)));
            }


            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<String> filteredList = (ArrayList<String>) results.values;
            if (results != null && results.count > 0) {
                synchronized (lock) {
                    clear();
                    for (String c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        }
    };
}
