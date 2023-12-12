package com.andreaak.cards.model;

public class VerbItem implements java.io.Serializable {

    private int id;

    public String _1;
    public String _1_Trans;
    public String _2;
    public String _2_Trans;
    public String _3;
    public String _3_Trans;
    public String _4;
    public String _4_Trans;
    public String translation;

    public VerbItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void addTag(String tag, String value) {
        switch (tag) {
            case "infinitive":
                _1 = value;
                break;
            case "infinitive_tr":
                _1_Trans = value;
                break;
            case "pastSimple":
                _2 = value;
                break;
            case "pastSimple_tr":
                _2_Trans = value;
                break;
            case "pastParticiple":
                _3 = value;
                break;
            case "pastParticiple_tr":
                _3_Trans = value;
                break;
            case "translation":
                translation = value;
                break;
        }
    }
}
