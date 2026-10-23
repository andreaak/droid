package com.andreaak.cards.model;

public enum Genus {
    MASCULINE("der"),
    FEMININE("die"),
    NEUTER("das"),
    UNKNOWN("");

    private final String article;

    Genus(String article) {
        this.article = article;
    }

    public String getArticle() {
        return article;
    }

    public String getPrefix() {
        return article.isEmpty() ? "" : article + " ";
    }

    public boolean isMasculine() {
        return this == MASCULINE;
    }

    public boolean isFeminine() {
        return this == FEMININE;
    }

    public boolean isNeuter() {
        return this == NEUTER;
    }

    public static Genus fromWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return UNKNOWN;
        }
        String lower = word.trim().toLowerCase();
        if (lower.startsWith("der ")) {
            return MASCULINE;
        } else if (lower.startsWith("die ")) {
            return FEMININE;
        } else if (lower.startsWith("das ")) {
            return NEUTER;
        }
        return UNKNOWN;
    }

    public static boolean isMasculine(String word) {
        return fromWord(word) == MASCULINE;
    }

    public static boolean isFeminine(String word) {
        return fromWord(word) == FEMININE;
    }

    public static boolean isNeuter(String word) {
        return fromWord(word) == NEUTER;
    }

    public static boolean isArticle(String value) {
        if (value == null) {
            return false;
        }
        String clean = value.trim().toLowerCase().replace("|", "");
        return "der".equals(clean) || "die".equals(clean) || "das".equals(clean);
    }
}
