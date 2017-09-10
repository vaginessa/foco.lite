package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import io.github.nfdz.foco.model.Document;

import static io.github.nfdz.foco.data.entity.DocumentEntity.TABLE_NAME;

/**
 * This Document implementation POJO class defines document fields to be stored in database.
 */
@Entity(tableName = TABLE_NAME)
public class DocumentEntity implements Document {

    public static final String TABLE_NAME = "documents";

    public static final String COLUMN_ID = BaseColumns._ID;

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_LAST_EDITION_TIME = "last_edition_time";

    public static final String COLUMN_TEXT = "text";

    public static final String COLUMN_FAVORITE = "favorite";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;

    @ColumnInfo(name = COLUMN_NAME)
    public String name;

    @ColumnInfo(name = COLUMN_LAST_EDITION_TIME)
    public long lastEditionTime = Document.NULL_LAST_EDITION_TIME;

    @ColumnInfo(name = COLUMN_TEXT)
    public String text = Document.NULL_TEXT;

    @ColumnInfo(name = COLUMN_FAVORITE)
    public boolean favorite = false;

    /** Default constructor */
    public DocumentEntity() {
    }

    /**
     * This constructor copy all fields of given document excepts ID field.
     * @param document
     */
    public DocumentEntity(Document document) {
        this.name = document.getName();
        this.lastEditionTime = document.getLastEditionTimeMillis();
        this.text = document.getText();
        this.favorite = document.isFavorite();
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
    public long getLastEditionTimeMillis() {
        return lastEditionTime;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

}
