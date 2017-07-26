package io.github.nfdz.foco.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public interface Document {

    long NULL_LAST_EDITION_TIME = -1;
    long NULL_WORKING_TIME = -1;
    String NULL_TEXT = "";
    int NULL_WORDS = 0;
    int NULL_COVER_COLOR = -1;
    String NULL_COVER_IMAGE = "";
    @ColorInt int DEFAULT_COVER_COLOR = Color.WHITE;

    long getId();
    String getName();
    long getWorkingTimeMillis();
    long getLastEditionTimeMillis();
    String getText();
    int getWords();
    boolean isFavorite();
    int getCoverColor();
    String getCoverImage();
}
