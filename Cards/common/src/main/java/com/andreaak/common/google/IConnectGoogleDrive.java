package com.andreaak.common.google;

public interface IConnectGoogleDrive {
    void onConnectionFail(Exception ex);

    void onConnectionOK();
}


