package com.andreaak.cards.activities;

public enum HideMode
{
    SHOW_ALL,
    HIDE_5,
    HIDE_4_5,
    HIDE_3_4_5,
    HIDE_2_3_4_5;

    public HideMode next() {

        switch (this) {

            case SHOW_ALL:
                return HIDE_5;

            case HIDE_5:
                return HIDE_4_5;

            case HIDE_4_5:
                return HIDE_3_4_5;

            case HIDE_3_4_5:
                return HIDE_2_3_4_5;

            default:
                return SHOW_ALL;
        }
    }
}
