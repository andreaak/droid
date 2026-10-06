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

public class SearchRuTextViewAdapter extends ArrayAdapter<SimpleWordItem> {

    private final Object lock =  new Object();

    private ArrayList<SimpleWordItem> items;
    private ArrayList<SimpleWordItem> itemsAll;
    private ArrayList<SimpleWordItem> suggestions;
    private String lang1;
    private String lang2;
    private int viewResourceId;

    private String currentLang = "lang2";

    @SuppressWarnings("unchecked")
    public SearchRuTextViewAdapter(Context context, int viewResourceId,
                                   ArrayList<SimpleWordItem> items, String lang1, String lang2) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<SimpleWordItem>) items.clone();
        this.suggestions = new ArrayList<>();
        this.viewResourceId = viewResourceId;
        this.lang1 = lang1;//ru
        this.lang2 = lang2;
        currentLang = lang2;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        SimpleWordItem item = items.get(position);
        if (item != null) {
            TextView itemLabel = (TextView)  v.findViewById(android.R.id.text1);
            if (itemLabel != null) {
                itemLabel.setText(item.getDisplayName(currentLang, lang1, lang2));
                itemLabel.setTextSize(20);
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
            String str = ((SimpleWordItem) (resultValue)).getDisplayName(currentLang, lang1, lang2);
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            synchronized (lock) {

                if (constraint != null) {
                    suggestions.clear();

                    String crt = constraint.toString();
                    currentLang = isRussian(crt) ? lang1 : lang2;

                    String ct = currentLang.equals(lang1) ?
                            Utils.normalizeRuForComparator(crt) :
                            Utils.normalizeForComparatorAndRemoveArtikles(crt);





                    for (SimpleWordItem item : itemsAll) {
                        String value = currentLang.equals(lang1) ?
                                Utils.normalizeRuForComparator(item.getValue(currentLang)) :
                                Utils.normalizeForComparatorAndRemoveArtikles(item.getValue(currentLang));

                        if (isContains(value, ct)) {
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
                        return a.getValue(lang2).compareTo(b.getValue(lang2));
                    }
                });

            }


            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        private boolean isRussian(String text) {

            boolean res =  text.matches("^[а-яА-ЯёЁ\\s\\p{P}]+$");
            return res;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            @SuppressWarnings("unchecked")
            ArrayList<SimpleWordItem> filteredList = (ArrayList<SimpleWordItem>) results.values;
            if (results != null && results.count > 0) {
                synchronized (lock) {
                    clear();
                    for (SimpleWordItem c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        }
    };

    private static boolean isContains(String value, String ct) {

        if(ct.startsWith("*")) {
            return value.endsWith(ct.substring(1));
        } else if(ct.endsWith("*")) {
            return value.startsWith(ct.substring(0, ct.length() - 1));
        } else if(ct.endsWith("@")) {
            return value.equals(ct.substring(0, ct.length() - 1));
        } else {
            return value.contains(ct);
        }
    }
}
