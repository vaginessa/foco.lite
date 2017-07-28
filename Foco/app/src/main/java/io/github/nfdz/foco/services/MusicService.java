package io.github.nfdz.foco.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

import io.github.nfdz.foco.data.MusicCatalog;
import timber.log.Timber;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final IBinder mMusicBind = new MusicBinder();

    private ArrayList<String> mSongs;
    private int mCurrentSong;
    private MediaPlayer mPlayer;

    public void onCreate(){
        super.onCreate();
        mCurrentSong = 0;
        mPlayer = new MediaPlayer();

        // set up media player
        mPlayer.setLooping(true);
        mPlayer.setVolume(1f, 1f);

        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);


        try {

            AssetFileDescriptor afd = getAssets().openFd(MusicCatalog.getInstance().getCatalog().get(1).getAssetPath());
            if (afd == null) return;
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.prepareAsync();
        } catch (IOException e) {
            Timber.d(e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int i = 0;
        i++;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {


        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        int i = 0;
        i++;

        mp.start();
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }

}
