package com.andreaak.cards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.andreaak.cards.R;
import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.activitiesShared.HandleExceptionActivity;
import com.andreaak.common.configs.SharedPreferencesHelper;
import com.andreaak.common.utils.Utils;
import com.andreaak.common.utils.logger.FileLogger;
import com.andreaak.common.utils.logger.ILogger;
import com.andreaak.common.utils.logger.Logger;
import com.andreaak.common.utils.logger.NativeLogger;

public class DownloadActivity extends HandleExceptionActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ((ImageButton) findViewById(R.id.buttonGDLessons)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonGDVerbs)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonGDIrregularVerbs)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonGDSounds)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.buttonGDGrammar)).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.buttonGDLessons)  {
            googleDriveLessons();
        } else if(id == R.id.buttonGDVerbs)  {
            googleDriveVerbs();
        } else if(id == R.id.buttonGDSounds)  {
            googleDriveSounds();
        } else if(id == R.id.buttonGDGrammar)  {
            googleDriveGrammar();
        } else if(id == R.id.buttonGDIrregularVerbs)  {
            googleDriveIrregularVerbs();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void googleDriveLessons() {
        Intent intent = new Intent(this, DriveActivity.class);
        intent.putExtra(DriveActivity.PATH, AppConfigs.getInstance().getLessonsDir() );
        intent.putExtra(DriveActivity.PREFIXES, AppConfigs.getInstance().LessonsPrefix);
        startActivity(intent);
    }

    private void googleDriveVerbs() {
        Intent intent = new Intent(this, DriveActivity.class);
        intent.putExtra(DriveActivity.PATH, AppConfigs.getInstance().getVerbDir());
        intent.putExtra(DriveActivity.PREFIXES, AppConfigs.getInstance().VerbPrefix);
        startActivity(intent);
    }

    private void googleDriveIrregularVerbs() {
        Intent intent = new Intent(this, DriveActivity.class);
        intent.putExtra(DriveActivity.PATH, AppConfigs.getInstance().getIrregularVerbDir());
        intent.putExtra(DriveActivity.PREFIXES, AppConfigs.getInstance().IrregularVerbPrefix);
        startActivity(intent);
    }

    private void googleDriveSounds() {
        Intent intent = new Intent(this, DriveSyncActivity.class);
        intent.putExtra(DriveSyncActivity.LOCAL_PATH, AppConfigs.getInstance().SoundsDir);
        intent.putExtra(DriveSyncActivity.DRIVE_PATH, AppConfigs.getInstance().getRemoteSoundsDir());
        startActivity(intent);
    }

    private void googleDriveGrammar() {
        Intent intent = new Intent(this, DriveSyncActivity.class);
        intent.putExtra(DriveSyncActivity.LOCAL_PATH, AppConfigs.getInstance().getGrammarDir());
        intent.putExtra(DriveSyncActivity.DRIVE_PATH, AppConfigs.getInstance().getRemoteGrammarDir());
        startActivity(intent);
    }
}

