package com.andreaak.common.google;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

import com.andreaak.common.R;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;

import static com.andreaak.common.utils.Utils.showText;

public class OperationGoogleDrive implements IOperationGoogleDrive {

    private Activity activity;
    private Menu menu;
    private String appName;
    private int group;

    public OperationGoogleDrive(Activity activity, String appName, int group) {
        this.activity = activity;
        this.appName = appName;
        this.group = group;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public void connectGoogleDrive(Intent data, Activity activity, GoogleDriveHelper googleDriveHelper) {
        activity.setTitle(R.string.connecting);
        if (data != null && data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) != null) {
            googleDriveHelper.getEmailHolder().setEmail(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            if (!googleDriveHelper.init()) {
                showText(activity, R.string.no_google_account);
                activity.setTitle(appName);
                Logger.d(Constants.LOG_TAG, activity.getString(R.string.no_google_account));
            } else {
                googleDriveHelper.connect();
            }
        } else {
            activity.setTitle(appName);
        }
    }

    @Override
    public void onConnectionOK() {
        menu.setGroupVisible(group, true);
        activity.setTitle(appName);
    }

    @Override
    public void onConnectionFail(Exception ex) {
        menu.setGroupVisible(group, false);
        Utils.showText(activity, R.string.google_error);
        activity.setTitle(appName);
        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
    }

    @Override
    public void onOperationProgress(String message) {
        activity.setTitle(message);
    }

    @Override
    public void onOperationFinished(Exception ex) {
        menu.setGroupVisible(group, true);
        if (ex == null) {
            Utils.showText(activity, R.string.download_success);
        } else {
            Utils.showText(activity, R.string.download_fault);
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
        }
        activity.setTitle(appName);
    }
}
