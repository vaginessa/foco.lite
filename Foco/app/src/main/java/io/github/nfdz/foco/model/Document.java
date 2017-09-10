package io.github.nfdz.foco.model;

/**
 * This interface defines all fields (through getter methods) that has a document in the app.
 */
public interface Document {

    // Default null values
    long NULL_ID = -1;
    long NULL_LAST_EDITION_TIME = -1;
    String NULL_TEXT = "";

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
     * Returns favorite document flag.
     * @return true if it is favorite, false if not
     */
    boolean isFavorite();

}
