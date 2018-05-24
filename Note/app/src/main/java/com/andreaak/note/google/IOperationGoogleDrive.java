package com.andreaak.note.google;

public interface IOperationGoogleDrive extends IConnectGoogleDrive {

    void onOperationFinished(Exception ex);

    void onOperationProgress(String message);
}
