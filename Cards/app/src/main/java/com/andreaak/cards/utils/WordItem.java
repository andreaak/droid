package com.andreaak.cards.utils;

public class WordItem {
    private String ru;
    private String en;
    private String en_tr;

    public String getRu() {
        return ru;
    }

    public String getEn_tr() {
        return en_tr;
    }

    public String getEn() {
        return en;
    }

    public WordItem(String ru, String en, String en_tr){
        this.ru = ru;
        this.en = en;
        this.en_tr = en_tr;
    }
}
