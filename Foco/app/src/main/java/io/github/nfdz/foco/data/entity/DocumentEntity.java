package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import io.github.nfdz.foco.model.Document;

import static io.github.nfdz.foco.data.entity.DocumentEntity.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
public class DocumentEntity implements Document {

    public static final String TABLE_NAME = "documents";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_WORKING_TIME = "working_time";

    public static final String COLUMN_LAST_EDITION_TIME = "last_edition_time";

    public static final String COLUMN_TEXT = "text";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public int id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_WORKING_TIME)
    public long workingTime;

    @ColumnInfo(name = COLUMN_LAST_EDITION_TIME)
    public long lastEditionTime;

    @ColumnInfo(name = COLUMN_TEXT)
    public String text;

    public DocumentEntity() {
    }

    public DocumentEntity(Document document) {
        this.id = document.getId();
        this.name = document.getName();
        this.workingTime = document.getWorkingTimeMillis();
        this.lastEditionTime = document.getLastEditionTimeMillis();
        this.text = document.getText();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getWorkingTimeMillis() {
        return workingTime;
    }

    public void setWorkingTimeMillis(long workingTime) {
        this.workingTime = workingTime;
    }

    @Override
    public long getLastEditionTimeMillis() {
        return lastEditionTime;
    }

    public void setLastEditionTimeMillis(long lastEditionTime) {
        this.lastEditionTime = lastEditionTime;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
