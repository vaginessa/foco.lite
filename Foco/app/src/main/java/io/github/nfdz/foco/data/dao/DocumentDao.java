package io.github.nfdz.foco.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;

import static io.github.nfdz.foco.data.entity.DocumentEntity.COLUMN_ID;
import static io.github.nfdz.foco.data.entity.DocumentEntity.COLUMN_LAST_EDITION_TIME;
import static io.github.nfdz.foco.data.entity.DocumentEntity.COLUMN_NAME;
import static io.github.nfdz.foco.data.entity.DocumentEntity.COLUMN_WORKING_TIME;
import static io.github.nfdz.foco.data.entity.DocumentEntity.TABLE_NAME;

@Dao
public interface DocumentDao {

    @Query("SELECT " + COLUMN_ID + ", " +COLUMN_NAME + ", " + COLUMN_WORKING_TIME +
            ", " + COLUMN_LAST_EDITION_TIME + " FROM " + TABLE_NAME)
    LiveData<List<DocumentMetadata>> loadAllDocumentsMetadata();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DocumentEntity document);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = :docId")
    LiveData<DocumentEntity> loadDocument(int docId);

    @Update
    void updateDocument(DocumentEntity document);

}
