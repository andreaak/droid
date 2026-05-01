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
import java.util.Comparator;

public class SearchTextViewAdapter extends ArrayAdapter<SimpleWordItem> {

    private ArrayList<SimpleWordItem> items;
    private ArrayList<SimpleWordItem> itemsAll;
    private ArrayList<SimpleWordItem> suggestions;
    private String lang;
    private int viewResourceId;

    @SuppressWarnings("unchecked")
    public SearchTextViewAdapter(Context context, int viewResourceId,
                                 ArrayList<SimpleWordItem> items, String lang) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<SimpleWordItem>) items.clone();
        this.suggestions = new ArrayList<>();
        this.viewResourceId = viewResourceId;
        this.lang = lang;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        SimpleWordItem product = items.get(position);
        if (product != null) {
            TextView productLabel = (TextView)  v.findViewById(android.R.id.text1);
            if (productLabel != null) {
                productLabel.setText(product.getDisplayName(lang));
                productLabel.setTextSize(20);
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        public String convertResultToString(Object resultValue) {
            String str = ((SimpleWordItem) (resultValue)).getDisplayName(lang);
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                suggestions.clear();
                String ct = Utils.normalizeForComparatorAndRemoveArtikles(constraint.toString());
                for (SimpleWordItem item : itemsAll) {
                    if (Utils.normalizeForComparatorAndRemoveArtikles(item.getValue(lang))
                            .contains(ct)) {
                        suggestions.add(item);
                        if(suggestions.size() >= 100) {
                            break;
                        }
                    }
                }

            } else {
                suggestions.clear();
                for (SimpleWordItem item : itemsAll) {
                    suggestions.add(item);
                    if(suggestions.size() >= 100) {
                        break;
                    }
                }
            }

            Collections.sort(suggestions, new Comparator<SimpleWordItem>() {
                @Override
                public int compare(SimpleWordItem a, SimpleWordItem b)
                {
                    return a.getValue(lang).compareTo(b.getValue(lang));
                }
            });

            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<SimpleWordItem> filteredList = (ArrayList<SimpleWordItem>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (SimpleWordItem c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
