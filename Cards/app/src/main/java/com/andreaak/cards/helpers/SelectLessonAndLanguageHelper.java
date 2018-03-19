package com.andreaak.cards.helpers;

import com.andreaak.cards.domain.LessonItem;

import java.io.File;

public class SelectLessonAndLanguageHelper implements java.io.Serializable {
    public File[] lessons;
    public File lessonFile;
    public LessonItem lessonItem;
    public boolean isRestore;
}
