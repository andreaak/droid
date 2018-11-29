package com.andreaak.cards.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.andreaak.common.utils.Constants;
import com.andreaak.common.utils.logger.Logger;

import java.util.Queue;

public class MediaPlayerHelper {

    public static final int DELAY_BETWEEN_WORDS = 500;

    private MediaPlayer mediaPlayer;
    private Context context;
    private Queue<String> files;
    public boolean IsActive;
    private int delay;

    public void playSound(Context context, Queue<String> files) {

        playSound(context, files, 0);
    }

    public void playSound(Context context, Queue<String> files, int delay) {

        if (IsActive) {
            return;
        }
        IsActive = true;
        this.files = files;
        this.context = context;
        this.delay = delay > 0 ? delay : DELAY_BETWEEN_WORDS;
        playSound(files.poll());
    }

    private boolean playSound(String filePath) {
        try {
            Uri sound = Uri.parse("file://" + filePath);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setDataSource(context, sound);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.start();
            return true;
        } catch (Exception ex) {
            Logger.e(Constants.LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
            IsActive = false;
            return false;
        }
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (files.isEmpty()) {
                clean();
            } else {
                reset();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                playSound(files.poll());
            }
        }
    };

    private void reset() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void clean() {
        reset();
        IsActive = false;
    }
}
