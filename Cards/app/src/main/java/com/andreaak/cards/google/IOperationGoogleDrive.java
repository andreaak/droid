package com.andreaak.cards.google;

public interface IOperationGoogleDrive extends IConnectGoogleDrive {

    void onOperationFinished(Exception ex);

    void onOperationProgress(String message);
}
