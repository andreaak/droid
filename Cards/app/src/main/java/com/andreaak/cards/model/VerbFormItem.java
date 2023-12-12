package com.andreaak.cards.model;

import java.io.File;

public class VerbFormItem implements java.io.Serializable {

    public String Single1;
    public String Single2;
    public String Single3;
    public String Plural1;
    public String Plural2;
    public String Plural3;
    public String Translation;

    public File file;

    public VerbFormType FormType;

    private int id;

    public VerbFormItem(int id, String ru) {
        this.id = id;
        Translation = ru;
    }

    public void addItem(String nodeTag, String value) {

        String tagValue = nodeTag.trim();

        switch (tagValue)
        {
            case "Type":
                VerbFormType val = VerbFormType.fromString(value);
                FormType = val == null ? VerbFormType.Imperativ : val;
                break;
            case "_1S":
                Single1 = value;
                break;
            case "_2S":
                Single2 = value;
                break;
            case "_3S":
                Single3 = value;
                break;
            case "_1P":
                Plural1 = value;
                break;
            case "_2P":
                Plural2 = value;
                break;
            case "_3P":
                Plural3 = value;
            break;
        }
    }
}

