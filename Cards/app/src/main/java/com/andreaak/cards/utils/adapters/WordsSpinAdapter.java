package com.andreaak.cards.utils.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.utils.WordItem;

import java.util.List;

public class WordsSpinAdapter extends ArrayAdapter<WordItem> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
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
    public int getCount(){
        return values.size();
    }

    @Override
    public WordItem getItem(int position){
        return values.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = new TextView(context);
        //label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).getValue(language));
        label.setTextSize(15);
        label.setPadding(2,5,2,5);

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        //label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getValue(language));
        label.setTextSize(15);
        label.setPadding(2,5,2,5);

        return label;
    }
}
