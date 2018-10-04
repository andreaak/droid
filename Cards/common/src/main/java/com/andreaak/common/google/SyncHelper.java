package com.andreaak.common.google;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

public class SyncHelper {

    private RootSyncItem rootSyncItem;
    private IOperationGoogleDrive searchActivity;

    public SyncHelper(String rootFolder, String remoteRootFolder, IOperationGoogleDrive searchActivity) {
        rootSyncItem = new RootSyncItem(rootFolder, remoteRootFolder);
        this.searchActivity = searchActivity;
    }

    @SuppressLint("StaticFieldLeak")
    public void process() {
        final boolean[] isDownload = {false};

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    publishProgress("Search started");
                    rootSyncItem.init();
                    publishProgress("Synchronization started");
                    boolean res = rootSyncItem.synchronize();
                    if (!res) {
                        throw new Exception("Download fault");
                    }
                    isDownload[0] = true;
                } catch (Exception ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    return ex;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... strings) {
                super.onProgressUpdate(strings);
                Logger.d(Constants.LOG_TAG, strings[0]);
                searchActivity.onOperationProgress(strings[0]);
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    searchActivity.onOperationFinished(null);
                } else {
                    searchActivity.onOperationFinished(ex);
                }
            }
        }.execute();
    }
}
