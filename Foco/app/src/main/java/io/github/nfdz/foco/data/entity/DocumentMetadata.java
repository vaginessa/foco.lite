package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;

public class DocumentMetadata {

    public int id;

    public String name;

    @ColumnInfo(name = "working_time")
    public long workingTime;

    @ColumnInfo(name = "last_edition_time")
    public long lastEditionTime;

}
