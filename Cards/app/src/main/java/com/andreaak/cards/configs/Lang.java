package com.andreaak.cards.configs;

public enum Lang
{
    DE,
    RU,
    EN;

    public static String getLanguage(Lang lg) {
        switch (lg) {
            case DE:
                return "de";
            case RU:
                return "ru";
            case EN:
                return "en";
        }
        return "";
    };

    public static Lang getLanguage(String lg) {

        return Lang.valueOf(lg.toUpperCase());
    };
}
