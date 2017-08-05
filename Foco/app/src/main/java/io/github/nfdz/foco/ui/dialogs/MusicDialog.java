package io.github.nfdz.foco.ui.dialogs;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.MusicCatalog;
import io.github.nfdz.foco.model.Song;
import io.github.nfdz.foco.services.MusicService;

/**
 * This class is a DialogFragment implementation that binds to music service,
 * shows the current state of it and allows user to interact with the service from its interface.
 */
public class MusicDialog extends DialogFragment implements MusicService.MusicCallback {

    @BindView(R.id.dialog_music_rv) RecyclerView mRecyclerView;
    @BindView(R.id.dialog_music_controls) View mControls;
    @BindView(R.id.dialog_music_mute) ImageButton mMuteButton;
    @BindView(R.id.dialog_music_loop) ImageButton mLoopButton;
    @BindView(R.id.dialog_music_play) ImageButton mPlayButton;

    private SongsAdapter mAdapter;
    private Intent mServiceStarter;
    private MusicConnection mServiceConnection;
    private MusicService mMusicService;

    public static MusicDialog newInstance() {
        return new MusicDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_music, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), orientation, reverseLayout);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SongsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // hide whole interface until the fragment is connected with the service
        hideContent();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title and set background color
        // (thus the integration of old and new sdk versions is better)
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        return dialog;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // dismiss dialog when configuration changes
        // they are not expected to live long and thus replicate the behavior of
        // the rest of the application dialogs (mostly based on AlertDialog)
        if (savedInstanceState != null) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // bind service
        hideContent();
        mServiceStarter = new Intent(getActivity(), MusicService.class);
        mServiceConnection = new MusicConnection();
        getActivity().bindService(mServiceStarter, mServiceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(mServiceStarter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMusicService != null) mMusicService.setCallback(null);
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onPlayMusic(int songPos) {
        mPlayButton.setImageResource(R.drawable.ic_pause);
        mPlayButton.setContentDescription(getString(R.string.dialog_music_pause_description));
        mAdapter.selectSong(songPos);
        mRecyclerView.smoothScrollToPosition(songPos);
    }

    @Override
    public void onStopMusic() {
        mAdapter.selectSong(-1);
        mPlayButton.setImageResource(R.drawable.ic_play);
        mPlayButton.setContentDescription(getString(R.string.dialog_music_play_description));
    }

    @Override
    public void onPauseMusic(int songPos) {
        mPlayButton.setImageResource(R.drawable.ic_play);
        mPlayButton.setContentDescription(getString(R.string.dialog_music_play_description));
        mAdapter.selectSong(songPos);
        mRecyclerView.smoothScrollToPosition(songPos);
    }

    @Override
    public void onMuteMusic(boolean muted) {
        mMuteButton.setSelected(mMusicService.isMuted());
    }

    @Override
    public void onLoopingMusic(boolean looping) {
        mLoopButton.setSelected(mMusicService.isLooping());
    }

    private void hideContent() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mControls.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mControls.setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.dialog_music_close)
    public void onCloseClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_music_play)
    public void onPlayClick() {
        if (mMusicService.isPlaying()) {
            mMusicService.pause();
        } else {
            mMusicService.resume();
            //mMusicService.playSong(mMusicService.getCurrentSong());
        }
    }

    @OnClick(R.id.dialog_music_stop)
    public void onStopClick() {
        mMusicService.stop();
    }

    @OnClick(R.id.dialog_music_mute)
    public void onMuteClick() {
        mMusicService.setMuted(!mMusicService.isMuted());
    }

    @OnClick(R.id.dialog_music_loop)
    public void onLoopClick() {
        mMusicService.setLooping(!mMusicService.isLooping());
    }

    public void songPicked(int songPos){
        mMusicService.playSong(songPos);
    }

    public class SongsAdapter extends RecyclerView.Adapter<SongViewHolder> {

        private List<Song> mCatalog = MusicCatalog.getInstance(getActivity()).getCatalog();
        private int mSelectedSong = -1;

        public void selectSong(int selectedSong) {
            mSelectedSong = selectedSong;
            notifyDataSetChanged();
        }

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId = R.layout.song_list_item;
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            boolean shouldAttachToParent = false;
            View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
            return new SongViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SongViewHolder holder, int position) {
            Song song = mCatalog.get(position);
            holder.mIcon.setImageResource(song.getArt());
            holder.mTitle.setText(song.getTitle());
            holder.itemView.setSelected(mSelectedSong == position);
        }

        @Override
        public int getItemCount() {
            return mCatalog.size();
        }
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.song_item_icon) ImageView mIcon;
        @BindView(R.id.song_item_title) TextView mTitle;

        public SongViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            songPicked(getAdapterPosition());
        }
    }

    private class MusicConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            mMusicService = binder.getService();
            mMusicService.setCallback(MusicDialog.this);

            // get initial data and update interface with current state: play/pause, mute, looping
            int currentSong = mMusicService.getCurrentSong();
            mAdapter.selectSong(currentSong);
            mRecyclerView.smoothScrollToPosition(currentSong);
            if (mMusicService.isPlaying()) {
                mPlayButton.setContentDescription(getString(R.string.dialog_music_pause_description));
                mPlayButton.setImageResource(R.drawable.ic_pause);
            } else {
                mPlayButton.setContentDescription(getString(R.string.dialog_music_play_description));
                mPlayButton.setImageResource(R.drawable.ic_play);
            }
            mLoopButton.setSelected(mMusicService.isLooping());
            mMuteButton.setSelected(mMusicService.isMuted());

            showContent();

            // it must refresh recycler view content because there is a problem with item width
            // sometimes (it does not fill according with match_parent)
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hideContent();
            mMusicService = null;
        }
    }
}