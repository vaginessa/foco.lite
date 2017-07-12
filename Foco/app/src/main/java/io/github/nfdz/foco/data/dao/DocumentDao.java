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

@Dao
public interface DocumentDao {

    @Query("SELECT id, name, working_time, last_edition_time FROM documents")
    LiveData<List<DocumentMetadata>> loadAllDocumentsMetadata();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DocumentEntity document);

    @Query("SELECT * FROM documents WHERE id = :docId")
    LiveData<DocumentEntity> loadDocument(int docId);

    @Update
    void updateDocument(DocumentEntity document);

}
