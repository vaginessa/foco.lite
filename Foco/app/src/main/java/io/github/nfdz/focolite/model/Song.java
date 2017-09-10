package io.github.nfdz.focolite.model;


import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * Song POJO class. It implements Comparable interface with title criteria.
 */
public class Song implements Comparable<Song> {

    private final String mTitle;
    private final @DrawableRes int mArt;
    private final String mAssetPath;

    public Song(@NonNull String title, @NonNull @DrawableRes int art, @NonNull String assetPath) {
        mTitle = title;
        mArt = art;
        mAssetPath = assetPath;
    }

    /**
     * Returns title of song string.
     * @return title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns drawable resource integer ID of the art of the song.
     * @return drawable resource
     */
    public int getArt() {
        return mArt;
    }

    /**
     * Returns raw audio file asset path string.
     * @return asset path
     */
    public String getAssetPath() {
        return mAssetPath;
    }

    @Override
    public int compareTo(@NonNull Song o) {
        return getTitle().compareTo(o.getTitle());
    }
}
