package io.github.nfdz.foco.data;


import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nfdz.foco.R;
import io.github.nfdz.foco.model.Song;
import io.reactivex.annotations.NonNull;

/**
 * This singleton class defines the available song resources.
 */
public class MusicCatalog {

    /**
     * Inner static class that defines a song resource regardless of the specific platform.
     */
    public static class SongResource {
        public final @StringRes int titleRes;
        public final @DrawableRes int artRes;
        public final String assetPath;

        public SongResource(@StringRes int titleRes, @DrawableRes int artRes, String assetPath) {
            this.titleRes = titleRes;
            this.artRes = artRes;
            this.assetPath = assetPath;
        }
    }

    /**
     * Available song resources hardcoded array.
     */
    public static final SongResource[] SONGS = {
            new SongResource(R.string.song_raindrops, R.drawable.ic_rain, "music/nature/raindrops-noise.ogg"),
            new SongResource(R.string.song_fireplace, R.drawable.ic_fire, "music/nature/fireplace-sound.ogg"),
            new SongResource(R.string.song_storm, R.drawable.ic_storm, "music/nature/approaching-storm.ogg"),
            new SongResource(R.string.song_waves, R.drawable.ic_waves, "music/nature/crashing-waves.ogg"),
            new SongResource(R.string.song_farm, R.drawable.ic_cow, "music/nature/farm-ambience.ogg"),
            new SongResource(R.string.song_ocean_music, R.drawable.ic_sailboat, "music/nature/ocean-music.ogg"),
            new SongResource(R.string.song_forest, R.drawable.ic_trees, "music/nature/relaxing-forest-sounds.ogg"),
            new SongResource(R.string.song_ocean_sounds, R.drawable.ic_starfish, "music/nature/sounds-of-the-ocean.ogg"),
            new SongResource(R.string.song_thunder, R.drawable.ic_storm, "music/nature/thunder-rumble-ambience-sound.ogg"),
            new SongResource(R.string.song_water_stream, R.drawable.ic_water_drop, "music/nature/water-stream.ogg"),
            new SongResource(R.string.song_metro, R.drawable.ic_metro, "music/city/metro-inside-sound.ogg"),
            new SongResource(R.string.song_traffic, R.drawable.ic_car, "music/city/traffic-noise.ogg")
    };

    // singleton instantiation
    private static MusicCatalog sInstance;
    private static final Object LOCK = new Object();
    public synchronized static MusicCatalog getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new MusicCatalog(context);
                }
            }
        }
        return sInstance;
    }

    private final List<Song> mCatalog;

    /**
     * Default constructor. It retrieves all strings using given context, build Song catalog List
     * and sort it (default comparator that has title string criteria).
     * @param context
     */
    public MusicCatalog(Context context) {
        // sort songs by natural order (title) and initialize
        List<Song> songs = new ArrayList<>();
        for (SongResource songRes : SONGS) {
            songs.add(new Song(context.getString(songRes.titleRes), songRes.artRes, songRes.assetPath));
        }
        Collections.sort(songs);
        mCatalog = Collections.unmodifiableList(songs);
    }

    /**
     * It returns Song catalog List. It could be empty but not empty.
     * @return List<Song>
     */
    @NonNull
    public List<Song> getCatalog() {
        return mCatalog;
    }

}
