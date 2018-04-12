package com.andreaak.cards.activities.helpers;

import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.model.VerbLessonItem;

import java.io.Serializable;

public class VerbActivityHelper implements Serializable {
    public VerbLessonItem lessonItem;
    public VerbItem currentWord;
    public boolean isRestore;
}
