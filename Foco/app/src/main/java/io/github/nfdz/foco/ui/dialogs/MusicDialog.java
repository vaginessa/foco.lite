package io.github.nfdz.foco.ui.dialogs;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.MusicCatalog;
import io.github.nfdz.foco.model.Song;

public class MusicDialog extends DialogFragment {

    @BindView(R.id.dialog_music_rv) RecyclerView mRecyclerView;

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
        mRecyclerView.setAdapter(new SongsAdapter());

        //view.findViewById(R.id.dialog_music_play).setSelected(true);
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
        if (savedInstanceState != null) {
            getDialog().dismiss();
        }
    }

    public class SongsAdapter extends RecyclerView.Adapter<SongViewHolder> {

        private List<Song> mCatalog = MusicCatalog.getInstance().getCatalog();

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
            // TODO
        }
    }
}