package io.github.nfdz.foco.model;


import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

public class Song implements Comparable<Song> {

    private final String mTitle;
    private final @DrawableRes int mArt;
    private final String mAssetPath;

    public Song(@NonNull String title, @NonNull @DrawableRes int art, @NonNull String assetPath) {
        mTitle = title;
        mArt = art;
        mAssetPath = assetPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getArt() {
        return mArt;
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    @Override
    public int compareTo(@NonNull Song o) {
        return getTitle().compareTo(o.getTitle());
    }
}
