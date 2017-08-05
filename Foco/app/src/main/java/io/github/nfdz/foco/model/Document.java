package io.github.nfdz.foco.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * This interface defines all fields (through getter methods) that has a document in the app.
 */
public interface Document {

    // Default null values
    long NULL_ID = -1;
    long NULL_LAST_EDITION_TIME = -1;
    long NULL_WORKING_TIME = -1;
    String NULL_TEXT = "";
    int NULL_WORDS = 0;
    int NULL_COVER_COLOR = -1;
    String NULL_COVER_IMAGE = "";
    @ColorInt int DEFAULT_COVER_COLOR = Color.WHITE;

    /**
     * Returns ID field.
     * @return long
     */
    long getId();

    /**
     * Returns name/title field.
     * @return name
     */
    String getName();

    /**
     * Returns working time field (duration in milliseconds).
     * @return working time
     */
    long getWorkingTimeMillis();

    /**
     * Returns last edition time field (epoch/unix timestamp in millis).
     * @return last edition time
     */
    long getLastEditionTimeMillis();

    /**
     * Returns document text.
     * @return text
     */
    String getText();

    /**
     * Returns the number of words of document text.
     * @return number of words
     */
    int getWords();

    /**
     * Returns favorite document flag.
     * @return true if it is favorite, false if not
     */
    boolean isFavorite();

    /**
     * Returns document cover color integer.
     * @return cover color
     */
    int getCoverColor();

    /**
     * Returns document cover image path.
     * @return cover image path
     */
    String getCoverImage();
}
