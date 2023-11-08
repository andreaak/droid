package com.andreaak.cards.model;

public class DeVerbItem extends VerbItem{

    public DeVerbItem(int id) {
        super(id);
    }

    public void addTag(String tag, String value) {
        switch (tag) {
            case "infinitive":
                _1 = value;
                break;
            case "infinitive_tr":
                _1_Trans = value;
                break;
            case "prasens":
                _2 = value;
                break;
            case "prasens_tr":
                _2_Trans = value;
                break;
            case "prateritum":
                _3 = value;
                break;
            case "prateritum_tr":
                _3_Trans = value;
                break;
            case "partizip":
                _4 = value;
                break;
            case "partizip_tr":
                _4_Trans = value;
                break;
            case "translation":
                translation = value;
                break;
        }
    }
}