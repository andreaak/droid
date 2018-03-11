package com.andreaak.cards.google;

public interface IConnectGoogleDrive {
    void onConnectionFail(Exception ex);

    void onConnectionOK();

    void onDownloadFinished(Exception ex);

    void onDownloadProgress(String message);
}


