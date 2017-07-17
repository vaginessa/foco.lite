package io.github.nfdz.foco.model;

public interface Document {

    long NULL_LAST_EDITION_TIME = -1;
    long NULL_WORKING_TIME = -1;
    String NULL_TEXT = "";
    int NULL_WORDS = -1;

    long getId();
    String getName();
    long getWorkingTimeMillis();
    long getLastEditionTimeMillis();
    String getText();
    int getWords();
    boolean isFavorite();
}
