package com.andreaak.cards.model;

public enum VerbFormType {
    Prasens("Prasens"),
    Prateritum("Prateritum"),
    Perfect("Perfect"),
    Plusquamperfekt("Plusquamperfekt"),
    FuturI("FuturI"),
    FuturII("FuturII"),
    KonjunktivIPrasens("KonjunktivIPrasens"),
    KonjunktivIPerfect("KonjunktivIPerfect"),
    KonjunktivIFuturI("KonjunktivIFuturI"),
    KonjunktivIFuturII("KonjunktivIFuturII"),
    KonjunktivIIPrateritum("KonjunktivIIPrateritum"),
    KonjunktivIIPlusquamperfekt("KonjunktivIIPlusquamperfekt"),
    KonjunktivIIFuturI("KonjunktivIIFuturI"),
    KonjunktivIIFuturII("KonjunktivIIFuturII"),
    Imperativ("Imperativ");

    private String text;

    VerbFormType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static VerbFormType fromString(String text) {
        for (VerbFormType b : VerbFormType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
