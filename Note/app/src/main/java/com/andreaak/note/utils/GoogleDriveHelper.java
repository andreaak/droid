package com.andreaak.note.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GoogleDriveHelper {

    EmailHolder emailHolder;

    private static GoogleDriveHelper instance;

    public static void initInstance(EmailHolder emailHolder) {
        instance = new GoogleDriveHelper(emailHolder);
    }

    public static GoogleDriveHelper getInstance() {
        return instance;
    }

    private GoogleDriveHelper(EmailHolder emailHolder) {
        this.emailHolder = emailHolder;
    }

    public interface ConnectCBs {
        void onConnFail(Exception ex);

        void onConnOK();
    }

    private static Drive service;
    private static ConnectCBs mConnCBs;
    private static boolean isConnected;

    /************************************************************************************************
     * initialize Google Drive Api
     *
     * @param act activity context
     */
    public boolean init(Activity act) {
        if (act != null) try {
            String email = emailHolder.getEmail();
            if (email != null) {
                mConnCBs = (ConnectCBs) act;
                service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                        GoogleAccountCredential.usingOAuth2(UT.acx, Collections.singletonList(DriveScopes.DRIVE))
                                .setSelectedAccountName(email)
                ).build();
                return true;
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
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
                    } catch (UserRecoverableAuthIOException e) {  // standard authorization failure - user fixable
                        Log.e(Constants.LOG_TAG, e.getMessage(), e);
                        return e;
                    } catch (GoogleAuthIOException e) {  // usually PackageName /SHA1 mismatch in DevConsole
                        Log.e(Constants.LOG_TAG, e.getMessage(), e);
                        return e;
                    } catch (IOException e) {   // '404 not found' in FILE scope, consider connected
                        if (e instanceof GoogleJsonResponseException) {
                            if (404 == ((GoogleJsonResponseException) e).getStatusCode())
                                isConnected = true;
                        }
                    } catch (Exception e) {  // "the name must not be empty" indicates
                        Log.e(Constants.LOG_TAG, e.getMessage(), e);           // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception ex) {
                    super.onPostExecute(ex);
                    if (isConnected) {
                        mConnCBs.onConnOK();
                    } else {  // null indicates general error (fatal)
                        mConnCBs.onConnFail(ex);
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

    /************************************************************************************************
     * find file/folder in GOODrive
     *
     * @param prnId parent ID (optional), null searches full drive, "root" searches Drive root
     * @param titl  file/folder name (optional)
     * @param mime  file/folder mime type (optional)
     * @return arraylist of found objects
     */
    public ArrayList<ContentValues> search(String prnId, String titl, String mime) {
        ArrayList<ContentValues> gfs = new ArrayList<>();
        if (service != null && isConnected) try {
            // add query conditions, build query
            String qryClause = "'me' in owners and ";
            if (prnId != null) qryClause += "'" + prnId + "' in parents and ";
            if (titl != null) qryClause += "title = '" + titl + "' and ";
            if (mime != null) qryClause += "mimeType = '" + mime + "' and ";
            qryClause = qryClause.substring(0, qryClause.length() - " and ".length());
            Drive.Files.List qry = service.files().list().setQ(qryClause)
                    .setFields("items(id,mimeType,labels/trashed,title),nextPageToken");
            String npTok = null;
            if (qry != null) do {
                FileList gLst = qry.execute();
                if (gLst != null) {
                    for (File gFl : gLst.getItems()) {
                        if (gFl.getLabels().getTrashed()) continue;
                        gfs.add(UT.newCVs(gFl.getTitle(), gFl.getId(), gFl.getMimeType()));
                    }                                                                 //else UT.lg("failed " + gFl.getTitle());
                    npTok = gLst.getNextPageToken();
                    qry.setPageToken(npTok);
                }
            }
            while (npTok != null && npTok.length() > 0);                     //UT.lg("found " + vlss.size());
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return gfs;
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
            meta.setMimeType(UT.MIME_FLDR);

            File gFl = null;
            try {
                gFl = service.files().insert(meta).execute();
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage(), e);
            }
            if (gFl != null && gFl.getId() != null) {
                rsId = gFl.getId();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
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
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return rsId;
    }

    public boolean saveToFile(String resId, java.io.File file) {
        if (service != null && isConnected && resId != null) try {
            File gFl = service.files().get(resId).setFields("downloadUrl").execute();
            if (gFl != null) {
                String strUrl = gFl.getDownloadUrl();
                return UT.saveToFile(service.getRequestFactory().buildGetRequest(new GenericUrl(strUrl)).execute().getContent(), file);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return false;
    }

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

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
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
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * FILE / FOLDER type object inquiry
     *
     * @param cv oontent values
     * @return TRUE if FOLDER, FALSE otherwise
     */
    public boolean isFolder(ContentValues cv) {
        String mime = cv.getAsString(UT.MIME);
        return mime != null && UT.MIME_FLDR.equalsIgnoreCase(mime);
    }

}

