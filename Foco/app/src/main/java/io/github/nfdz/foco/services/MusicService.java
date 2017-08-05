package io.github.nfdz.foco.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import java.io.IOException;

import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.MusicCatalog;
import io.github.nfdz.foco.model.Song;
import io.github.nfdz.foco.ui.MainActivity;
import timber.log.Timber;

/**
 * This class is a bounded service that manages the playing of ambient music.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    // intent actions
    public static final String STOP_MUSIC_ACTION = "STOP_MUSIC";
    public static final String PLAY_MUSIC_ACTION = "PLAY_MUSIC";
    public static final String PAUSE_MUSIC_ACTION = "PAUSE_MUSIC";
    public static final String NEXT_SONG_ACTION = "NEXT_SONG";
    public static final String PREVIOUS_SONG_ACTION = "PREVIOUS_SONG";

    private static final int NOTIFICATION_ID = 8948;

    /**
     * Service callback interface to be implemented to receive music events.
     */
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

        // ensure that there is no old notification
        stopNotification();
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(STOP_MUSIC_ACTION)) {
                stop();
            } else if (action.equals(PLAY_MUSIC_ACTION)) {
                resume();
            } else if (action.equals(PAUSE_MUSIC_ACTION)) {
                pause();
            } else if (action.equals(PREVIOUS_SONG_ACTION)) {
                playPreviousSong();
            }else if (action.equals(NEXT_SONG_ACTION)) {
                playNextSong();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * This method initializes music player object. It sets volume, looping flag and etc.
     */
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
        stopNotification();
        // ensure that removes music player reference in order to avoid memory leaks
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    /**
     * Simple implementation inner class with single method that exposes the whole service.
     */
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mCallback = null; // to avoid memory leaks
        return true;
    }

    public void setCallback(MusicCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.e("On music player error: what=" + what + ", extra=" + extra);
        return true; // if this is false it will call onCompletion method
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (mCallback != null) mCallback.onPlayMusic(mCurrentSong);
        updateNotification();
    }

    /**
     * This method plays given song (contained in music catalog and retrieved
     * using its position there).
     * @param songPos song position in music catalog.
     */
    public void playSong(int songPos){
        if (mPlayer.isPlaying()) mPlayer.stop();

        // restart music player
        mPlayer.reset();
        initMediaPlayer();

        mCurrentSong = songPos;
        try {
            final Song song = MusicCatalog.getInstance(this).getCatalog().get(mCurrentSong);
            AssetFileDescriptor afd = getAssets().openFd(song.getAssetPath());
            if (afd == null) {
                Timber.e("Cannot open asset file descriptor for song = "+song.getAssetPath());
            } else {
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mPlayer.prepareAsync();
            }
        } catch (IOException e) {
            Timber.d(e);
        }
    }

    /**
     * This method stops current song if it is playing.
     */
    public void stop() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayerInitialized = false;
        if (mCallback != null) mCallback.onStopMusic();
        stopNotification();
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
        return mPlayerInitialized && mPlayer.isPlaying();
    }

    /**
     * This method plays the next song.
     */
    public void playNextSong() {
        int nextSong = mCurrentSong + 1;
        if (nextSong >= MusicCatalog.getInstance(this).getCatalog().size()) {
            nextSong = 0;
        }
        playSong(nextSong);
    }

    /**
     * This method plays the previous song.
     */
    public void playPreviousSong() {
        int previousSong = mCurrentSong - 1;
        if (previousSong < 0) {
            previousSong =  MusicCatalog.getInstance(this).getCatalog().size() - 1;
        }
        playSong(previousSong);
    }

    /**
     * This method resumes playback of the song. If it is not playing nothing (paused state),
     * it starts to play the last played song (stopped state).
     */
    public void resume() {
        if (mPlayerInitialized) {
            mPlayer.start();
            if (mCallback != null) mCallback.onPlayMusic(mCurrentSong);
            updateNotification();
        } else {
            playSong(mCurrentSong);
        }
    }

    /**
     * This method pauses current song.
     */
    public void pause() {
        if (mPlayer.isPlaying()) mPlayer.pause();
        if (mCallback != null) mCallback.onPauseMusic(mCurrentSong);
        updateNotification();
    }

    /**
     * This method clears the notification of music service.
     */
    private void stopNotification() {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    /**
     * this method updates the notification of music service with the current state.
     */
    private void updateNotification() {
        final Song song = MusicCatalog.getInstance(this).getCatalog().get(mCurrentSong);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        Intent mainStarter = new Intent(this, MainActivity.class);
        mainStarter.setAction(MainActivity.OPEN_MUSIC_ACTION);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainStarter, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentIntent(mainPendingIntent)
                .setContentTitle(song.getTitle())
                .setContentText(getString(R.string.notification_text))
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        // stop music pending intent
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(STOP_MUSIC_ACTION);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // set actions (only for lollipop and newer version because there are a problems with
        // vector drawables icons in older versions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // force to use actions because they are more explicit that notification swipe
            notificationBuilder.setOngoing(true);

            int playPauseButtonPosition = 1;
            int stopPosition = 3;

            // previous song position -> 0
            Intent prevIntent = new Intent(this, MusicService.class);
            prevIntent.setAction(PREVIOUS_SONG_ACTION);
            PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action prevAction = new NotificationCompat.Action.Builder(R.drawable.ic_previous_song,
                    getString(R.string.notification_previous_action),
                    prevPendingIntent).build();
            notificationBuilder.addAction(prevAction);

            // play/pause position -> 1
            if (isPlaying()) {
                Intent pauseIntent = new Intent(this, MusicService.class);
                pauseIntent.setAction(PAUSE_MUSIC_ACTION);
                PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action pauseAction = new NotificationCompat.Action.Builder(R.drawable.ic_pause,
                        getString(R.string.notification_pause_action),
                        pausePendingIntent).build();
                notificationBuilder.addAction(pauseAction);
            } else {
                Intent playIntent = new Intent(this, MusicService.class);
                playIntent.setAction(PLAY_MUSIC_ACTION);
                PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action playAction = new NotificationCompat.Action.Builder(R.drawable.ic_play,
                        getString(R.string.notification_play_action),
                        playPendingIntent).build();
                notificationBuilder.addAction(playAction);
            }

            // next song position -> 2
            Intent nextIntent = new Intent(this, MusicService.class);
            nextIntent.setAction(NEXT_SONG_ACTION);
            PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action nextAction = new NotificationCompat.Action.Builder(R.drawable.ic_next_song,
                    getString(R.string.notification_next_action),
                    nextPendingIntent).build();
            notificationBuilder.addAction(nextAction);

            // stop position -> 3
            NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_stop,
                    getString(R.string.notification_stop_action),
                    stopPendingIntent).build();
            notificationBuilder.addAction(stopAction);

            // set compact view style with only play/pause and stop actions
            notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(new int[]{ playPauseButtonPosition, stopPosition }));
        } else {
            // delete (swipe notification)
            notificationBuilder.setDeleteIntent(stopPendingIntent);
        }

        // notify
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

}
