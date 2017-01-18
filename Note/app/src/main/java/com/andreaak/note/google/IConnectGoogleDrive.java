package com.andreaak.note.google;

public interface IConnectGoogleDrive {
    void onConnectionFail(Exception ex);

    void onConnectionOK();

    void onDownloadOK();

    void onDownloadFail(Exception ex);
}


