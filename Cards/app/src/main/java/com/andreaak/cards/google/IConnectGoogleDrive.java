package com.andreaak.cards.google;

public interface IConnectGoogleDrive {
    void onConnectionFail(Exception ex);

    void onConnectionOK();
}


