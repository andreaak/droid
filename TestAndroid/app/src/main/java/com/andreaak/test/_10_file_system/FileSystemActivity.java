package com.andreaak.test._10_file_system;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.andreaak.test.R;

public class FileSystemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_10_file_system);

        TextView textView = (TextView) findViewById(R.id.textView);
        String text = "Root:\t" + Environment.getRootDirectory() +
                "\nDownload Cache Dir:\t" + Environment.getDownloadCacheDirectory() +
                "\nData Directory:\t" + Environment.getDataDirectory() +

                "\nExternal Storage State:\t" + Environment.getExternalStorageState() +
                "\nisExternal Storage Removable:\t" + Environment.isExternalStorageRemovable() +
                "\nExternal Storage Dir:\t" + Environment.getExternalStorageDirectory() +

                "\n\nExternal Storage Public Directory:\t" +
                "\n\tAlarms:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS) +
                "\n\tDCIM:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                "\n\tDownloads:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                "\n\tMovies:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +
                "\n\tMusic:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) +
                "\n\tNotification:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS) +
                "\n\tPictures:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                "\n\tPodcasts:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS) +
                "\n\tRingtones:\t" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES) +

                "\n\nApplication dir:\t" + this.getFilesDir();

        textView.append(text);
    }
}
