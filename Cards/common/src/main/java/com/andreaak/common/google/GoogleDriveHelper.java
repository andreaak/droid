package com.andreaak.common.google;

import android.app.Activity;
import android.os.AsyncTask;

import com.andreaak.common.R;
import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.Logger;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.andreaak.common.utils.Constants.LOG_TAG;

public class GoogleDriveHelper {

    private static GoogleDriveHelper instance;
    //https://developers.google.com/drive/api/v3/mime-types
    public final String MIME_TEXT = "text/plain";
    public final static String MIME_FLDR = "application/vnd.google-apps.folder";
    private Drive service;
    private IOperationGoogleDrive operationGoogleDrive;
    private boolean isConnected;
    private EmailHolder emailHolder;

    private Activity activity;

    public void setActivity(Activity activity, IOperationGoogleDrive operationGoogleDrive) {
        this.activity = activity;
        this.operationGoogleDrive = operationGoogleDrive;
    }

    private GoogleDriveHelper(EmailHolder emailHolder) {
        this.emailHolder = emailHolder;
    }

    public static void initInstance(EmailHolder emailHolder) {
        instance = new GoogleDriveHelper(emailHolder);
    }

    public static GoogleDriveHelper getInstance() {
        return instance;
    }

    public EmailHolder getEmailHolder() {
        return emailHolder;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean init() {
        try {
            String email = emailHolder.getEmail();
            if (email != null) {

                service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                        GoogleAccountCredential.usingOAuth2(Utils.acx, Collections.singletonList(DriveScopes.DRIVE))
                                .setSelectedAccountName(email)
                ).build();
                Logger.d(LOG_TAG, "Google drive initialized");
                return true;
            }
        } catch (Exception ex) {
            Logger.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * connect
     */
    public void connect() {
        if (emailHolder.getEmail() != null && service != null) {
            isConnected = false;
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... nadas) {
                    try {
                        // GoogleAuthUtil.getToken(mAct, email, DriveScopes.DRIVE_FILE);   SO 30122755
                        service.files().get("root").setFields("title").execute();
                        isConnected = true;
                        Logger.d(LOG_TAG, "Google drive connected");
                    } catch (UserRecoverableAuthIOException ex) {  // standard authorization failure - user fixable
                        Logger.e(LOG_TAG, ex.getMessage(), ex);
                        ex.printStackTrace();
                        return ex;
                    } catch (GoogleAuthIOException ex) {  // usually PackageName /SHA1 mismatch in DevConsole
                        Logger.e(LOG_TAG, ex.getMessage(), ex);
                        ex.printStackTrace();
                        return ex;
                    } catch (IOException ex) {   // '404 not found' in FILE scope, consider connected
                        ex.printStackTrace();
                        if (ex instanceof GoogleJsonResponseException) {
                            if (404 == ((GoogleJsonResponseException) ex).getStatusCode()) {
                                isConnected = true;
                                Logger.d(LOG_TAG, "Google drive connected");
                            }

                        } else {
                            Logger.e(LOG_TAG, ex.getMessage(), ex);
                            return ex;
                        }
                    } catch (Exception ex) {  // "the name must not be empty" indicates
                        Logger.e(LOG_TAG, ex.getMessage(), ex);           // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
                        ex.printStackTrace();
                        return ex;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception ex) {
                    super.onPostExecute(ex);
                    if (isConnected) {
                        operationGoogleDrive.onConnectionOK();
                    } else {  // null indicates general error (fatal)
                        operationGoogleDrive.onConnectionFail(ex);
                    }
                }
            }.execute();
        }
    }

    /**
     * disconnect    disconnects GoogleApiClient
     */
    public void disconnect() {
    }

    public GoogleItem searchFolder(String parentId, String relativePath) {
        GoogleItem result = null;
        String[] titles = relativePath.split("/");
        for (String title : titles) {
            ArrayList<GoogleItem> items = search(parentId, title, MIME_FLDR);

            if (items.isEmpty()) {
                result = null;
                break;
            }
            result = items.get(0);
            parentId = result.getId();
        }
        return result;
    }

    /************************************************************************************************
     * find file/folder in GOODrive
     *
     * @param parentId parent ID (optional), null searches full drive, "root" searches Drive root
     * @param title    file/folder name (optional)
     * @param mime     file/folder mime type (optional)
     * @return arraylist of found objects
     */
    public ArrayList<GoogleItem> search(String parentId, String title, String mime) {
        ArrayList<GoogleItem> result = new ArrayList<GoogleItem>();
        if (service != null && isConnected) {
            try {
                // add query conditions, build query
                // String qryClause = "'me' in owners and ";

                StringBuilder sb = new StringBuilder();
                AddClause(sb, "'me' in owners");
                if (parentId != null) {
                    AddClause(sb, String.format("'%1$s' in parents", parentId));
                    //qryClause += "'" + prnId + "' in parents and ";
                }
                if (title != null) {
                    AddClause(sb, String.format("title = '%1$s'", title));
                    //qryClause += "title = '" + titl + "' and ";
                }
                if (mime != null) {
                    AddClause(sb, String.format("mimeType = '%1$s'", mime));
                    //qryClause += "mimeType = '" + mime + "' and ";
                }
                //qryClause = qryClause.substring(0, qryClause.length() - " and ".length());
                Drive.Files.List qry = service.files().list().setQ(sb.toString())
                        .setFields("items(id,mimeType,labels/trashed,title,modifiedDate),nextPageToken");

                if (qry == null) {
                    return result;
                }

                String pageToken = null;
                do {
                    FileList files = qry.execute();
                    if (files != null) {
                        for (File file : files.getItems()) {
                            if (file.getLabels().getTrashed())
                                continue;
                            GoogleItem item = new GoogleItem(file);
                            result.add(item);
                        }
                        pageToken = files.getNextPageToken();
                        qry.setPageToken(pageToken);
                    }
                }
                while (pageToken != null && pageToken.length() > 0);

            } catch (Exception ex) {
                Logger.e(LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
            }
        }
        return result;
    }

    private void AddClause(StringBuilder sb, String clause) {
        if (sb.length() != 0) {
            sb.append(" and ");
        }
        sb.append(clause);
    }

    /************************************************************************************************
     * create file/folder in GOODrive
     *
     * @param prnId parent's ID, (null or "root") for root
     * @param titl  file name
     * @return file id  / null on fail
     */
    public String createFolder(String prnId, String titl) {
        String rsId = null;
        if (service != null && isConnected && titl != null) try {
            File meta = new File();
            meta.setParents(Collections.singletonList(new ParentReference().setId(prnId == null ? "root" : prnId)));
            meta.setTitle(titl);
            meta.setMimeType(MIME_FLDR);

            File gFl = null;
            try {
                gFl = service.files().insert(meta).execute();
            } catch (Exception ex) {
                Logger.e(LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
            }
            if (gFl != null && gFl.getId() != null) {
                rsId = gFl.getId();
            }
        } catch (Exception ex) {
            Logger.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return rsId;
    }

    /************************************************************************************************
     * create file/folder in GOODrive
     *
     * @param prnId parent's ID, (null or "root") for root
     * @param titl  file name
     * @param mime  file mime type
     * @param file  file (with content) to create
     * @return file id  / null on fail
     */
    public String createFile(String prnId, String titl, String mime, java.io.File file) {
        String rsId = null;
        if (service != null && isConnected && titl != null && mime != null && file != null) try {
            File meta = new File();
            meta.setParents(Collections.singletonList(new ParentReference().setId(prnId == null ? "root" : prnId)));
            meta.setTitle(titl);
            meta.setMimeType(mime);

            File gFl = service.files().insert(meta, new FileContent(mime, file)).execute();
            if (gFl != null)
                rsId = gFl.getId();
        } catch (Exception ex) {
            Logger.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return rsId;
    }

    public boolean saveToFile2(String resId, java.io.File file) {
        if (service != null && isConnected && resId != null)
            try {
                File googleFile = service.files().get(resId).setFields("downloadUrl").execute();
                if (googleFile != null) {
                    String strUrl = googleFile.getDownloadUrl();
                    long size = googleFile.getFileSize();
                    return Utils.saveToFile(service.getRequestFactory().buildGetRequest(new GenericUrl(strUrl)).execute().getContent(), file);
                }
            } catch (Exception ex) {
                Logger.e(LOG_TAG, ex.getMessage(), ex);
                ex.printStackTrace();
            }
        return false;
    }
//
//    public static boolean saveToFile(long size, InputStream is, java.io.File file) {
//        if (!Utils.checkOrCreateFolderForFile(file)) {
//            return false;
//        }
//
//        BufferedInputStream bufferedStream = null;
//        if (is != null) try {
//            int bufferSize = 4096;
//            FileOutputStream fileOutput = new FileOutputStream(file);
//            bufferedStream = new BufferedInputStream(is);
//            byte[] buffer = new byte[bufferSize];
//            int bufferLength = 0;
//
//            long downloadSize = 0;
//            while ((bufferLength = bufferedStream. read(buffer)) > 0) {
//                fileOutput.write(buffer, 0, bufferLength);
//                downloadSize += bufferSize;
//                //publishProgress(activity.getString(R.string.download_file) + " " + item.getTitle());
//            }
//            fileOutput.close();
//            return true;
//        } catch (Exception ex) {
//            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
//            ex.printStackTrace();
//        } finally {
//            try {
//                if (bufferedStream != null) bufferedStream.close();
//            } catch (Exception ex) {
//                Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
//                ex.printStackTrace();
//            }
//        }
//        return false;
//    }

    /************************************************************************************************
     * update file in GOODrive,  see https://youtu.be/r2dr8_Mxr2M (WRONG?)
     * see https://youtu.be/r2dr8_Mxr2M   .... WRONG !!!
     *
     * @param resId file  id
     * @param titl  new file name (optional)
     * @param mime  new mime type (optional, "application/vnd.google-apps.folder" indicates folder)
     * @param file  new file content (optional)
     * @return file id  / null on fail
     */
    public String update(String resId, String titl, String mime, String desc, java.io.File file) {
        File gFl = null;
        if (service != null && isConnected && resId != null) try {
            File meta = new File();
            if (titl != null) meta.setTitle(titl);
            if (mime != null) meta.setMimeType(mime);
            if (desc != null) meta.setDescription(desc);

            if (file == null)
                gFl = service.files().patch(resId, meta).execute();
            else
                gFl = service.files().update(resId, meta, new FileContent(mime, file)).execute();

        } catch (Exception ex) {
            Logger.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return gFl == null ? null : gFl.getId();
    }

    /************************************************************************************************
     * trash file in GOODrive
     *
     * @param resId file  id
     * @return success status
     */
    public boolean trash(String resId) {
        if (service != null && isConnected && resId != null) try {
            return null != service.files().trash(resId).execute();
        } catch (Exception ex) {
            Logger.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * FILE / FOLDER type object inquiry
     *
     * @param items oontent values
     * @return TRUE if FOLDER, FALSE otherwise
     */

    public void saveFiles(final GoogleItems items, final String path) {
        final boolean[] isDownload = {false};
        new AsyncTask<Void, String, Exception>() {
            @Override
            protected Exception doInBackground(Void... nadas) {
                try {
                    boolean res = true;

                    for (GoogleItem item : items.getItems()) {
                        publishProgress(activity.getString(R.string.download_file) + " " + item.getTitle());
                        java.io.File targetFile = new java.io.File(path + "/" + item.getTitle());
                        res = saveToFile(item.getTitle(), item.getId(), targetFile) && res;
                    }
                    isDownload[0] = res;
                } catch (Exception ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                    return ex;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... strings) {
                super.onProgressUpdate(strings);
                Logger.d(LOG_TAG, strings[0]);
                operationGoogleDrive.onOperationProgress(strings[0]);
            }

            @Override
            protected void onPostExecute(Exception ex) {
                super.onPostExecute(ex);
                if (isDownload[0]) {
                    operationGoogleDrive.onOperationFinished(null);
                } else {
                    Exception e = ex != null ? ex : new Exception(activity.getString(R.string.download_fault));
                    operationGoogleDrive.onOperationFinished(e);
                }
            }

            public boolean saveToFile(String fileName, String resId, java.io.File file) {
                if (service != null && isConnected && resId != null)
                    try {
                        File googleFile = service.files().get(resId).setFields("downloadUrl, fileSize").execute();
                        if (googleFile != null) {
                            String strUrl = googleFile.getDownloadUrl();
                            long size = googleFile.getFileSize();
                            return saveToFile(fileName, size, service.getRequestFactory().buildGetRequest(new GenericUrl(strUrl)).execute().getContent(), file);
                        }
                    } catch (Exception ex) {
                        Logger.e(LOG_TAG, ex.getMessage(), ex);
                        ex.printStackTrace();
                    }
                return false;
            }

            public boolean saveToFile(String fileName, long size, InputStream is, java.io.File file) {
                if (!Utils.checkOrCreateFolderForFile(file)) {
                    return false;
                }

                BufferedInputStream bufferedStream = null;
                if (is != null) try {
                    int bufferSize = 4096;
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    bufferedStream = new BufferedInputStream(is);
                    byte[] buffer = new byte[bufferSize];
                    int bufferLength = 0;

                    long downloadSize = 0;
                    while ((bufferLength = bufferedStream. read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadSize += bufferSize;
                        publishProgress(fileName + " " + (int)((double)downloadSize/size*100)+ "%");
                    }
                    fileOutput.close();
                    return true;
                } catch (Exception ex) {
                    Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                    ex.printStackTrace();
                } finally {
                    try {
                        if (bufferedStream != null) bufferedStream.close();
                    } catch (Exception ex) {
                        Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
                        ex.printStackTrace();
                    }
                }
                return false;
            }
        }.execute();
    }
}

