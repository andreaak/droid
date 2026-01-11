package com.andreaak.cards.model;

import android.text.Html;

import java.util.Comparator;

public class WordsComparator implements Comparator<WordItem> {

    String language;


    public WordsComparator(String language) {
        this.language  = language;
    }

    @Override
    public int compare(WordItem word1, WordItem word2)
    {
        String value1 =  normalize(word1.getValue(language));
        String value2 =  normalize(word2.getValue(language));
        return  value1.compareTo(value2);
    }

    private String normalize(String value) {

        value = Html.fromHtml(value).toString();

        return value.replace("der ", "")
                .replace("die ", "")
                .replace("das ", "")
                .replace("der(die) ", "")
                .replace("die(der) ", "")
                .replace("der(das) ", "");
    }
}
