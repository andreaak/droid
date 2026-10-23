package com.andreaak.cards.model;

public enum Lang {
    DE("de"),
    RU("ru"),
    EN("en");

    private final String code;

    Lang(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Возвращает строковый код языка для любого переданного объекта Lang.
     * При добавлении новых языков достаточно добавить значение в enum с соответствующим кодом.
     */
    public static String getLanguageCode(Lang lg) {
        if (lg == null) {
            return EN.getCode();
        }
        return lg.getCode();
    }

    public static String getLanguage(Lang lg) {
        return getLanguageCode(lg);
    }

    public static Lang getLanguage(String lg) {
        if (lg == null || lg.trim().isEmpty()) {
            return EN;
        }
        String clean = lg.trim();
        for (Lang l : values()) {
            if (l.code.equalsIgnoreCase(clean) || l.name().equalsIgnoreCase(clean)) {
                return l;
            }
        }
        return EN;
    }

    public static boolean isRu(String langCode) {
        return RU.code.equalsIgnoreCase(langCode);
    }
}
