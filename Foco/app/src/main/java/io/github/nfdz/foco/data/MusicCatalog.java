package io.github.nfdz.foco.data;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.nfdz.foco.R;
import io.github.nfdz.foco.model.Song;
import io.reactivex.annotations.NonNull;

public class MusicCatalog {

    public static final Song[] SONGS = {
            new Song("Raindrops noise", R.drawable.ic_rain, "music/nature/raindrops-noise.ogg"),
            new Song("Fireplace", R.drawable.ic_fire, "music/nature/fireplace-sound.ogg"),
            new Song("Approaching storm", R.drawable.ic_storm, "music/nature/approaching-storm.ogg"),
            new Song("Crashing waves", R.drawable.ic_waves, "music/nature/crashing-waves.ogg"),
            new Song("Farm ambience", R.drawable.ic_cow, "music/nature/farm-ambience.ogg"),
            new Song("Ocean music", R.drawable.ic_starfish, "music/nature/ocean-music.ogg"),
            new Song("Forest sounds", R.drawable.ic_trees, "music/nature/relaxing-forest-sounds.ogg"),
            new Song("Ocean sounds", R.drawable.ic_starfish, "music/nature/sounds-of-the-ocean.ogg"),
            new Song("Thunder rumble sounds", R.drawable.ic_storm, "music/nature/thunder-rumble-ambience-sound.ogg"),
            new Song("Water stream", R.drawable.ic_water_drop, "music/nature/water-stream.ogg"),
            new Song("Building construction", R.drawable.ic_constructor, "music/city/building-construction-sound-effect.ogg"),
            new Song("Metro inside sound", R.drawable.ic_metro, "music/city/metro-inside-sound.ogg"),
            new Song("Traffic noise", R.drawable.ic_car, "music/city/traffic-noise.ogg"),
            new Song("Classic music", R.drawable.ic_piano, "music/classic/classic-music.ogg"),
    };

    // singleton instantiation
    private static MusicCatalog sInstance;
    private static final Object LOCK = new Object();
    public synchronized static MusicCatalog getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new MusicCatalog();
                }
            }
        }
        return sInstance;
    }

    private final List<Song> mCatalog;

    public MusicCatalog() {
        // sort songs by natural order (title) and initialize map
        List<Song> songs = new ArrayList<>(Arrays.asList(SONGS));
        Collections.sort(songs);
        mCatalog = Collections.unmodifiableList(songs);
    }

    @NonNull
    public List<Song> getCatalog() {
        return mCatalog;
    }


}
