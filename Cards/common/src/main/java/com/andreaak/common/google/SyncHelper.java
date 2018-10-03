package com.andreaak.common.google;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.andreaak.common.R;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

public class SyncHelper {

    private RootSyncItem rootSyncItem;
    private IGoogleSearch searchActivity;

    public SyncHelper(String rootFolder, String remoteRootFolder, IGoogleSearch searchActivity) {
        rootSyncItem = new RootSyncItem(rootFolder, remoteRootFolder);
        this.searchActivity = searchActivity;
    }

    @SuppressLint("StaticFieldLeak")
    private void fill(final ISyncItem item) {
        final boolean[] isDownload = {false};

        new AsyncTask<Void, String, Exception>() {

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    item.init();
                    publishProgress("Search Completed");
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
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    searchActivity.onSearchFinished(null);
                } else {
                    searchActivity.onSearchFinished(ex);
                }
            }
        }.execute();
    }
}
