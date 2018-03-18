package com.andreaak.cards.utils;

import java.io.File;
import java.util.ArrayList;

public class SelectCardsActivityHelper implements java.io.Serializable {
    public File[] lessons;
    public File lessonFile;
    public ArrayList<WordItem> words;
    public LanguageItem language;
    public String currentLanguage;
    public boolean isRestore;
}
