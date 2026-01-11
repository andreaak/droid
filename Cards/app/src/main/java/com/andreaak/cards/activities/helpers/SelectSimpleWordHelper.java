package com.andreaak.cards.activities.helpers;

import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.model.WordItem;

import java.util.ArrayList;

public class SelectSimpleWordHelper implements java.io.Serializable {
    public ArrayList<WordItem> items;
    public WordItem currentItem;
    public boolean isRestore;
}
