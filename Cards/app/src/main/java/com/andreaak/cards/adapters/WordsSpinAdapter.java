package com.andreaak.cards.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.model.WordsComparator;
import com.andreaak.common.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WordsSpinAdapter extends ArrayAdapter<WordItem> {

    public static final String ALL = "All";
    public static final String A1B2 = "A1-B2";

    private static List<String> Levels = Arrays.asList(new String[]{"A1", "A2", "B1", "B2", "C1", "C2"});

    private Context context;
    private List<WordItem> sourceWords;
    private List<WordItem> values;
    private String language;
    public String level;
    public boolean sort;

    public WordsSpinAdapter(Context context, int textViewResourceId,
                            ArrayList<WordItem> values, boolean sort, String language) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.sourceWords = this.values = (ArrayList<WordItem>)values.clone();
        this.language = language;

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
    public WordItem getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(WordItem item) {
        return values.indexOf(item);
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

    public boolean setLevel(String level) {
        if(Utils.isEqual(this.level, level)) {
            return false;
        }

        this.level = level;
        values = GetWordsForLevel(sourceWords, language, level);
        return true;
    }

    public List<WordItem> setSort(boolean sort) {
        if(this.sort == sort) {
            return values;
        }

        this.sort = sort;

        if(!sort) {
            values = GetWordsForLevel(sourceWords, language, level);
        } else {

            List<WordItem> copy = GetClonedWordsForLevel(sourceWords, language, level);
            Collections.sort(copy, new WordsComparator(language));
            values = copy;
        }

        return values;
    }

    boolean isShuffle = false;

    public List<WordItem> shuffle() {

        isShuffle = !isShuffle;
        if(!isShuffle) {
            values = GetWordsForLevel(sourceWords, language, level);
        } else {

            List<WordItem> copy = GetClonedWordsForLevel(sourceWords, language, level);
            Collections.shuffle(copy);
            values = copy;
        }

        return values;
    }

    public List<WordItem> GetWords(String language, String level) {
        return GetWordsForLevel(sourceWords, language, level);
    }

    private List<WordItem> GetWordsForLevel(List<WordItem> words, String language, String level) {
        if(ALL.equals(level) || Utils.isEmpty(level)) {
            return words;
        }

        ArrayList<WordItem> list = new ArrayList<>();

        for(WordItem w : words) {
            String l = w.getLevel(language);

            if(isFiltered(level, l)) {
                list.add(w);
            }
        }

        return list;
    }

    private List<WordItem> GetClonedWordsForLevel(List<WordItem> words, String language, String level) {
        if(ALL.equals(level) || Utils.isEmpty(level)) {
            return new ArrayList<>(sourceWords);
        }

        ArrayList<WordItem> list = new ArrayList<>();

        for(WordItem w : words) {
            String l = w.getLevel(language);

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
