package com.andreaak.cards.model;

public class VerbItem implements java.io.Serializable {

    private int id;

    public String infinitive;
    public String infinitiveTrans;
    public String pastSimple;
    public String pastSimpleTrans;
    public String pastParticiple;
    public String pastParticipleTrans;
    public String translation;

    public VerbItem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
