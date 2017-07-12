package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;

public class DocumentMetadata {

    @ColumnInfo(name = DocumentEntity.COLUMN_ID)
    public int id;

    @ColumnInfo(name = DocumentEntity.COLUMN_NAME)
    public String name;

    @ColumnInfo(name = DocumentEntity.COLUMN_WORKING_TIME)
    public long workingTime;

    @ColumnInfo(name = DocumentEntity.COLUMN_LAST_EDITION_TIME)
    public long lastEditionTime;

}
