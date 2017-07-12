package io.github.nfdz.foco.model;

public interface Document {
    int getId();
    String getName();
    long getWorkingTimeMillis();
    long getLastEditionTimeMillis();
    String getText();
}
