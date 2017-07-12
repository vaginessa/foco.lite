package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import io.github.nfdz.foco.model.Document;

@Entity(tableName = "documents")
public class DocumentEntity implements Document {

    @PrimaryKey
    public int id;

    public String name;

    @ColumnInfo(name = "working_time")
    public long workingTime;

    @ColumnInfo(name = "last_edition_time")
    public long lastEditionTime;

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
