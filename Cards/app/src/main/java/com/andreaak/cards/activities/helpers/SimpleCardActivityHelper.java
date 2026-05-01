package com.andreaak.cards.activities.helpers;

import com.andreaak.cards.model.LanguageItem;
import com.andreaak.cards.model.SimpleWordItem;
import com.andreaak.cards.model.WordItem;

import java.io.Serializable;

public class SimpleCardActivityHelper implements Serializable {
    public LanguageItem language;
    public SimpleWordItem currentSimpleWord;
    public WordItem currentWord;
}
