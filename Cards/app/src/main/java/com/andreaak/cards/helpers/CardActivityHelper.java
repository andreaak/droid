package com.andreaak.cards.helpers;

import com.andreaak.cards.domain.LessonItem;
import com.andreaak.cards.domain.WordItem;

import java.io.Serializable;

public class CardActivityHelper implements Serializable {
    public LessonItem lessonItem;
    public WordItem currentWord;
    public boolean isRestore;
}
