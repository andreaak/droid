package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchTextViewAdapter extends ArrayAdapter<WordItem> {

    private ArrayList<WordItem> items;
    private ArrayList<WordItem> itemsAll;
    private ArrayList<WordItem> suggestions;
    private String lang;
    private int viewResourceId;

    @SuppressWarnings("unchecked")
    public SearchTextViewAdapter(Context context, int viewResourceId,
                                 ArrayList<WordItem> items, String lang) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<WordItem>) items.clone();
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
        WordItem product = items.get(position);
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
            if (constraint != null) {
                suggestions.clear();
                String ct = Utils.normalizeForComparatorAndRemoveArtikles(constraint.toString());
                for (WordItem item : itemsAll) {
                    if (Utils.normalizeForComparatorAndRemoveArtikles(item.getDisplayName(lang))
                            .contains(ct)) {
                        suggestions.add(item);
                        if(suggestions.size() >= 100) {
                            break;
                        }
                    }
                }

                Collections.sort(suggestions, new Comparator<WordItem>() {
                    @Override
                    public int compare(WordItem a, WordItem b)
                    {
                        return a.getValue("de").compareTo(b.getValue("de"));
                    }
                });

                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
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
