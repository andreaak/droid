package com.andreaak.cards.google;

public interface IGoogleSearch {

    void onSearchOk();

    void onSearchFail(Exception ex);
}