package com.andreaak.note.google;

public interface IGoogleSearch {

    void onSearchOk();

    void onSearchFail(Exception ex);
}