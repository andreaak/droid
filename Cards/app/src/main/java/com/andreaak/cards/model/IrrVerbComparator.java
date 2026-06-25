package com.andreaak.cards.model;

import android.text.Html;

import java.util.Comparator;

public class IrrVerbComparator implements Comparator<VerbItem> {

    @Override
    public int compare(VerbItem word1, VerbItem word2)
    {
        String value1 =  normalize(word1._1);
        String value2 =  normalize(word2._1);
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
