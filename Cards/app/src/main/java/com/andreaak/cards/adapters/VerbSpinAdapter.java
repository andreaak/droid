package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.IrrVerbComparator;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.WordItem;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VerbSpinAdapter extends ArrayAdapter<VerbItem> {
    public static final String ALL = "All";
    public static final String A1B2 = "A1-B2";

    private Context context;
    private List<VerbItem> values;
    private List<VerbItem> sourceWords;
    private static List<String> Levels = Arrays.asList(new String[]{"A1", "A2", "B1", "B2", "C1", "C2"});
    public String level;
    public boolean sort;

    public VerbSpinAdapter(Context context, int textViewResourceId,
                           ArrayList<VerbItem> values, boolean sort) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.sourceWords = this.values = (ArrayList<VerbItem>)values.clone();
        this.level = ALL;

        setSort(sort);
        if(sort) {
            clear();

            addAll(values);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public VerbItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(VerbItem item) {
        return values.indexOf(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = new TextView(context);
        label.setText(values.get(position)._1);
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values.get(position)._1);
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);

        return label;
    }

    public boolean setLevel(String level) {
        if(Utils.isEqual(this.level, level)) {
            return false;
        }

        this.level = level;
        values = GetWordsForLevel(sourceWords, level);
        return true;
    }

    boolean isShuffle = false;

    public List<VerbItem> shuffle() {

        isShuffle = !isShuffle;
        if(!isShuffle) {
            values = GetWordsForLevel(sourceWords, level);
        } else {

            List<VerbItem> copy = GetClonedWordsForLevel(sourceWords, level);
            Collections.shuffle(copy);
            values = copy;
        }

        return values;
    }

    public List<VerbItem> setSort(boolean sort) {
        if(this.sort == sort) {
            return values;
        }

        this.sort = sort;

        if(!sort) {
            values = GetWordsForLevel(sourceWords, level);
        } else {

            List<VerbItem> copy = GetClonedWordsForLevel(sourceWords, level);
            Collections.sort(copy, new IrrVerbComparator());
            values = copy;
        }

        return values;
    }
    private List<VerbItem> GetWordsForLevel(List<VerbItem> words, String level) {
        if(ALL.equals(level) || Utils.isEmpty(level)) {
            return words;
        }

        ArrayList<VerbItem> list = new ArrayList<>();

        for(VerbItem w : words) {
            String l = w.getLevel();

            if(isFiltered(level, l)) {
                list.add(w);
            }
        }

        return list;
    }

    private List<VerbItem> GetClonedWordsForLevel(List<VerbItem> words, String level) {
        if(ALL.equals(level) || Utils.isEmpty(level)) {
            return new ArrayList<>(sourceWords);
        }

        ArrayList<VerbItem> list = new ArrayList<>();

        for(VerbItem w : words) {
            String l = w.getLevel();

            if(isFiltered(level, l)) {
                list.add(w);
            }
        }

        return list;
    }

    private boolean isFiltered(String level, String l) {
        if(level.contains("-")) {
            if(isLevelInGroup(level, l)) {
                return true;
            }
        }
        else if(Utils.isEqual(level, l) || Utils.isEmpty(l) && "CC".equals(level)) {
            return true;
        }

        return false;
    }

    private boolean isLevelInGroup (String levelGroup, String level) {

        String[] values = levelGroup.split("-");
        int startIndex =  Levels.indexOf(values[0]);
        int endIndex =  Levels.indexOf(values[1]);

        int index =  Levels.indexOf(level);

        return startIndex <= index && index <= endIndex;
    }
}
