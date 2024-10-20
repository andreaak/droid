package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.andreaak.cards.model.LessonItem;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LessonsSpinAdapter extends ArrayAdapter<LessonItem> {

    private ArrayList<LessonItem> items;
    private ArrayList<LessonItem> itemsAll;
    private ArrayList<LessonItem> suggestions;
    private int viewResourceId;

    public LessonsSpinAdapter(Context context, int viewResourceId,
                              ArrayList<LessonItem> values) {
        super(context, viewResourceId, values);
        Collections.sort(values, new Comparator<LessonItem>() {
            @Override
            public int compare(LessonItem a, LessonItem b)
            {
                String namea = Utils.normalizeForComparator(a.getDisplayName());
                String nameb = Utils.normalizeForComparator(b.getDisplayName());
                return namea.compareTo(nameb);
            }
        });

        this.items = values;
        this.itemsAll = (ArrayList<LessonItem>) items.clone();
        this.suggestions = new ArrayList<LessonItem>();
        this.viewResourceId = viewResourceId;
    }

   public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        LessonItem product = items.get(position);
        if (product != null) {
            TextView productLabel = (TextView)  v.findViewById(android.R.id.text1);
            if (productLabel != null) {
                productLabel.setText(product.getDisplayName());
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
            String str = ((LessonItem) (resultValue)).getDisplayName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (LessonItem product : itemsAll) {
                    if (product.getDisplayName().toLowerCase()
                            .startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(product);
                    }
                }
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
            ArrayList<LessonItem> filteredList = (ArrayList<LessonItem>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (LessonItem c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}
