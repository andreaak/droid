package com.andreaak.note.google;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.andreaak.note.utils.Constants;
import com.andreaak.note.utils.Utils;
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

    private static GoogleDriveHelper instance;
    public final String MIME_TEXT = "text/plain";
    public final String MIME_FLDR = "application/vnd.google-apps.folder";
    //    public final String TITL = "titl";
//    public final String GDID = "gdid";
//    public final String MIME = "mime";
    private Drive service;
    private IConnectGoogleDrive connectInstance;
    private boolean isConnected;
    private EmailHolder emailHolder;

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

    public boolean init(Activity act) {
        if (act != null) try {
            String email = emailHolder.getEmail();
            if (email != null) {
                connectInstance = (IConnectGoogleDrive) act;
                service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                        GoogleAccountCredential.usingOAuth2(Utils.acx, Collections.singletonList(DriveScopes.DRIVE))
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
                        } else {
                            Log.e(Constants.LOG_TAG, e.getMessage(), e);
                            return e;
                        }
                    } catch (Exception e) {  // "the name must not be empty" indicates
                        Log.e(Constants.LOG_TAG, e.getMessage(), e);           // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception ex) {
                    super.onPostExecute(ex);
                    if (isConnected) {
                        connectInstance.onConnectionOK();
                    } else {  // null indicates general error (fatal)
                        connectInstance.onConnectionFail(ex);
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
     * @param id    parent ID (optional), null searches full drive, "root" searches Drive root
     * @param title file/folder name (optional)
     * @param mime  file/folder mime type (optional)
     * @return arraylist of found objects
     */
    public ArrayList<GoogleItem> search(String id, String title, String mime) {
        ArrayList<GoogleItem> result = new ArrayList<GoogleItem>();
        if (service != null && isConnected) {
            try {
                // add query conditions, build query
                // String qryClause = "'me' in owners and ";

                StringBuilder sb = new StringBuilder();
                AddClause(sb, "'me' in owners");
                if (id != null) {
                    AddClause(sb, String.format("'%1$s' in parents", id));
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
                        .setFields("items(id,mimeType,labels/trashed,title),nextPageToken");

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
                while (pageToken != null && pageToken.length() > 0);                     //Utils.lg("found " + vlss.size());

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage(), e);
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
        if (service != null && isConnected && resId != null)
            try {
                File googleFile = service.files().get(resId).setFields("downloadUrl").execute();
                if (googleFile != null) {
                    String strUrl = googleFile.getDownloadUrl();
                    return Utils.saveToFile(service.getRequestFactory().buildGetRequest(new GenericUrl(strUrl)).execute().getContent(), file);
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
    public boolean isFolder(GoogleItem cv) {
        String mime = cv.getMime();
        return mime != null && MIME_FLDR.equalsIgnoreCase(mime);
    }
}

