package io.github.nfdz.foco.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import io.github.nfdz.foco.data.dao.DocumentDao;
import io.github.nfdz.foco.data.entity.DocumentEntity;

@Database(entities = { DocumentEntity.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    static final String DATABASE_NAME = "foco.db";

    public abstract DocumentDao documentDao();

    // singleton instantiation
    private static AppDatabase sInstance;
    private static final Object LOCK = new Object();
    public synchronized static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    context.deleteDatabase(DATABASE_NAME);
                    sInstance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

}