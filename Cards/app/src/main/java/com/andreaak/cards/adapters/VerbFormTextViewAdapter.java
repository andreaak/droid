package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;

public class VerbFormTextViewAdapter extends ArrayAdapter<VerbForm> {

    private ArrayList<VerbForm> items;
    private ArrayList<VerbForm> itemsAll;
    private ArrayList<VerbForm> suggestions;
    private int viewResourceId;

    @SuppressWarnings("unchecked")
    public VerbFormTextViewAdapter(Context context, int viewResourceId,
                                   ArrayList<VerbForm> items) {
        super(context, viewResourceId, items);
        this.items = items;
        this.itemsAll = (ArrayList<VerbForm>) items.clone();
        this.suggestions = new ArrayList<VerbForm>();
        this.viewResourceId = viewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }
        VerbForm product = items.get(position);
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
            String str = ((VerbForm) (resultValue)).getDisplayName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                String ct = Utils.normalizeForComparator(constraint.toString().toLowerCase());
                for (VerbForm item : itemsAll) {
                    if (Utils.normalizeForComparator(item.getDisplayName().toLowerCase())
                            .contains(ct)) {
                        suggestions.add(item);
                        if(suggestions.size() >= 20) {
                            break;
                        }
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
            ArrayList<VerbForm> filteredList = (ArrayList<VerbForm>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (VerbForm c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}

