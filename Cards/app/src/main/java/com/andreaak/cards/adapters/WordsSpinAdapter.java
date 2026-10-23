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

    private static final List<String> Levels = Arrays.asList("A1", "A2", "B1", "B2", "C1", "C2");

    private final Context context;
    private final List<WordItem> sourceWords;
    private List<WordItem> values;
    private final String language;
    public String level;
    public String quantity;
    public boolean sort;
    private boolean isShuffle;

    public WordsSpinAdapter(Context context, int textViewResourceId,
                            ArrayList<WordItem> values, boolean sort, String language) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.sourceWords = new ArrayList<>(values);
        this.values = new ArrayList<>(values);
        this.language = language;

        this.level = ALL;
        this.quantity = ALL;
        this.sort = sort;

        applyFilters();
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public WordItem getItem(int position) {
        if (position >= 0 && position < values.size()) {
            return values.get(position);
        }
        return null;
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
        WordItem item = getItem(position);
        label.setText(item != null ? item.getValue(language) : "");
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        WordItem item = getItem(position);
        label.setText(item != null ? item.getValue(language) : "");
        label.setTextSize(15);
        label.setPadding(2, 2, 2, 2);
        return label;
    }

    public boolean setLevel(String level) {
        if (Utils.isEqual(this.level, level)) {
            return false;
        }
        this.level = level;
        applyFilters();
        return true;
    }

    public boolean setQuantity(String quantity) {
        if (Utils.isEqual(this.quantity, quantity)) {
            return false;
        }
        this.quantity = quantity;
        applyFilters();
        return true;
    }

    public List<WordItem> setSort(boolean sort) {
        this.sort = sort;
        applyFilters();
        return values;
    }

    public List<WordItem> shuffle() {
        this.isShuffle = !this.isShuffle;
        applyFilters();
        return values;
    }

    public void applyFilters() {
        List<WordItem> filtered = getWordsForLevelAndQuantity(sourceWords, language, level, quantity);
        if (sort) {
            Collections.sort(filtered, new WordsComparator(language));
        } else if (isShuffle) {
            Collections.shuffle(filtered);
        }
        this.values = filtered;
        clear();
        addAll(filtered);
        notifyDataSetChanged();
    }

    public List<WordItem> getWordsForLevel(String level) {
        return getWordsForLevel(sourceWords, language, level);
    }

    public static List<WordItem> getWordsForLevel(List<WordItem> words, String language, String level) {
        if (ALL.equals(level) || Utils.isEmpty(level)) {
            return new ArrayList<>(words);
        }

        ArrayList<WordItem> list = new ArrayList<>();
        for (WordItem w : words) {
            String l = w.getLevel(language);
            if (isLevelFiltered(level, l)) {
                list.add(w);
            }
        }
        return list;
    }

    public static List<WordItem> getWordsForLevelAndQuantity(List<WordItem> words, String language, String level, String quantity) {
        List<WordItem> levelFiltered = getWordsForLevel(words, language, level);
        if (ALL.equals(quantity) || Utils.isEmpty(quantity)) {
            return levelFiltered;
        }

        ArrayList<WordItem> list = new ArrayList<>();
        for (WordItem w : levelFiltered) {
            String q = w.getQuantity(language);
            if (isQuantityFiltered(quantity, q)) {
                list.add(w);
            }
        }
        return list;
    }

    private static boolean isLevelFiltered(String level, String l) {
        if (level.contains("-")) {
            if (isLevelInGroup(level, l)) {
                return true;
            }
        } else if (Utils.isEqual(level, l) || (Utils.isEmpty(l) && "CC".equals(level))) {
            return true;
        }
        return false;
    }

    private static boolean isLevelInGroup(String levelGroup, String level) {
        String[] values = levelGroup.split("-");
        int startIndex = Levels.indexOf(values[0]);
        int endIndex = Levels.indexOf(values[1]);
        int index = Levels.indexOf(level);
        return startIndex <= index && index <= endIndex;
    }

    public static boolean isQuantityFiltered(String filterQuantity, String wordQuantity) {
        if (ALL.equals(filterQuantity) || Utils.isEmpty(filterQuantity)) {
            return true;
        }
        String normFilter = normalizeQuantity(filterQuantity);
        String normWord = normalizeQuantity(wordQuantity);
        return Utils.isEqual(normFilter, normWord);
    }

    public static String normalizeQuantity(String q) {
        if (Utils.isEmpty(q) || "0".equals(q.trim())) {
            return "0";
        }
        return q.trim();
    }
}
