package io.github.nfdz.foco.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import io.github.nfdz.foco.data.dao.DocumentDao;
import io.github.nfdz.foco.data.entity.DocumentEntity;

@Database(entities = { DocumentEntity.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    static final String DATABASE_NAME = "foco";

    public abstract DocumentDao documentDao();

}