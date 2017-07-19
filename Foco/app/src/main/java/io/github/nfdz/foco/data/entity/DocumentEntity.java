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

    public static final String COLUMN_WORDS = "words";

    public static final String COLUMN_FAVORITE = "favorite";

    public static final String COLUMN_COVER_COLOR = "cover_color";

    public static final String COLUMN_COVER_IMAGE = "cover_image";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_WORKING_TIME)
    public long workingTime = Document.NULL_WORKING_TIME;

    @ColumnInfo(name = COLUMN_LAST_EDITION_TIME)
    public long lastEditionTime = Document.NULL_LAST_EDITION_TIME;

    @ColumnInfo(name = COLUMN_TEXT)
    public String text = Document.NULL_TEXT;

    @ColumnInfo(name = COLUMN_WORDS)
    public int words = Document.NULL_WORDS;

    @ColumnInfo(name = COLUMN_FAVORITE)
    public boolean favorite = false;

    @ColumnInfo(name = COLUMN_COVER_COLOR)
    public int coverColor = Document.NULL_COVER_COLOR;

    @ColumnInfo(name = COLUMN_COVER_IMAGE)
    public String coverImage = Document.NULL_COVER_IMAGE;

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
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getWorkingTimeMillis() {
        return workingTime;
    }

    @Override
    public long getLastEditionTimeMillis() {
        return lastEditionTime;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getWords() {
        return words;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public int getCoverColor() {
        return coverColor;
    }

    @Override
    public String getCoverImage() {
        return coverImage;
    }


}
