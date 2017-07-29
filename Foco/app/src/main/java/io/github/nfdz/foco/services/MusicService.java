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

import java.io.IOException;

import io.github.nfdz.foco.data.MusicCatalog;
import io.github.nfdz.foco.model.Song;
import timber.log.Timber;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public interface MusicCallback {
        void onStopMusic();
        void onPlayMusic(int songPos);
        void onPauseMusic(int songPos);
        void onMuteMusic(boolean muted);
        void onLoopingMusic(boolean looping);
    }

    private final IBinder mMusicBind = new MusicBinder();

    private int mCurrentSong;
    private MediaPlayer mPlayer;
    private boolean mPlayerInitialized;
    private boolean mIsMuted;
    private boolean mIsLooping;
    private MusicCallback mCallback;

    public void onCreate(){
        super.onCreate();
        mCurrentSong = 0;
        mPlayer = new MediaPlayer();
        mIsMuted = false;
        mIsLooping = true;
        mPlayerInitialized = false;
    }

    private void initMediaPlayer() {
        mPlayer.setLooping(mIsLooping);
        if (mIsMuted) {
            mPlayer.setVolume(0f, 0f);
        } else {
            mPlayer.setVolume(1f, 1f);
        }

        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayerInitialized = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mCallback = null; // to avoid memory leaks
        return super.onUnbind(intent);
    }

    public void setCallback(MusicCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int nextSong = mCurrentSong + 1;
        if (nextSong >= MusicCatalog.getInstance(this).getCatalog().size()) {
            nextSong = 0;
        }
        playSong(nextSong);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.e("On music player error: what=" + what + ", extra=" + extra);
        return true; // if this is false it will call onCompletion method
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void playSong(int songPos){
        if (mPlayer.isPlaying()) mPlayer.stop();
        mPlayer.reset();
        initMediaPlayer();

        mCurrentSong = songPos;
        try {
            Song song = MusicCatalog.getInstance(this).getCatalog().get(mCurrentSong);
            AssetFileDescriptor afd = getAssets().openFd(song.getAssetPath());
            if (afd == null) {
                Timber.e("Cannot open asset file descriptor for song = "+song.getAssetPath());
            } else {
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mPlayer.prepareAsync();
                if (mCallback != null) mCallback.onPlayMusic(mCurrentSong);
            }
        } catch (IOException e) {
            Timber.d(e);
        }
    }

    public void stop() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayerInitialized = false;
        }
        if (mCallback != null) mCallback.onStopMusic();
    }

    public int getCurrentSong() {
        return mCurrentSong;
    }

    public boolean isMuted() {
        return mIsMuted;
    }

    public void setMuted(boolean muted) {
        mIsMuted = muted;
        if (muted) {
            mPlayer.setVolume(0f, 0f);
        } else {
            mPlayer.setVolume(1f, 1f);
        }
        if (mCallback != null) mCallback.onMuteMusic(mIsMuted);
    }

    public boolean isLooping() {
        return mIsLooping;
    }

    public void setLooping(boolean looping) {
        mIsLooping = looping;
        if (mPlayer.isPlaying()) mPlayer.setLooping(mIsLooping);
        if (mCallback != null) mCallback.onLoopingMusic(mIsLooping);

    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void resume() {
        if (mPlayerInitialized) {
            mPlayer.start();
        } else {
            playSong(mCurrentSong);
        }
        if (mCallback != null) mCallback.onPlayMusic(mCurrentSong);
    }

    public void pause() {
        if (mPlayer.isPlaying()) mPlayer.pause();
        if (mCallback != null) mCallback.onPauseMusic(mCurrentSong);
    }
}
