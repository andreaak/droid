package com.andreaak.cards.utils;

import com.andreaak.cards.model.SimpleWordItem;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

    protected static Cache instance;

    private static HashMap<String, ArrayList<SimpleWordItem>> wordItems = new HashMap<>();
    private static HashMap<String, String> items = new HashMap<>();


    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public void add(String key, ArrayList<SimpleWordItem> items) {
        wordItems.put(key, items);
    }

    public void add(String key, String value) {
        items.put(key, value);
    }

    public ArrayList<SimpleWordItem> getWordItems(String key) {
        return wordItems.get(key);
    }

    public String getItem(String key) {
        return items.get(key);
    }
}
