package com.andreaak.cards.activities.helpers;

import com.andreaak.cards.model.LessonItem;
import com.andreaak.cards.model.WordItem;

import java.io.Serializable;

public class CardActivityHelper implements Serializable {
    public LessonItem lessonItem;
    public WordItem currentWord;
    public boolean isRestore;
}

