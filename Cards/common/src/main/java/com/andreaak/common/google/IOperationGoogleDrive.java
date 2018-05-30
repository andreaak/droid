package com.andreaak.common.google;

public interface IOperationGoogleDrive extends IConnectGoogleDrive {

    void onOperationFinished(Exception ex);

    void onOperationProgress(String message);
}
