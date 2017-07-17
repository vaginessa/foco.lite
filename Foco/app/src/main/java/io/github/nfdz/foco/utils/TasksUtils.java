package io.github.nfdz.foco.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.nfdz.foco.data.AppDatabase;
import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Callbacks;

public class TasksUtils {

    public static void createDocument(final Context context,
                                      final String name,
                                      final Callbacks.FinishCallback<Long> callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void[] params) {
                DocumentEntity doc = new DocumentEntity();
                doc.name = name;
                return AppDatabase.getInstance(context).documentDao().insert(doc)[0];
            }
            @Override
            protected void onPostExecute(Long id) {
                callback.onFinish(id);
            }
        }.execute();
    }

    public static void deleteDocument(final Context context,
                                      final Set<DocumentMetadata> docsToDelete,
                                      final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                List<DocumentEntity> documents = new ArrayList<DocumentEntity>();
                for (DocumentMetadata doc : docsToDelete) {
                    DocumentEntity entity = new DocumentEntity();
                    entity.id = doc.id;
                    documents.add(entity);
                }
                AppDatabase.getInstance(context).documentDao().delete(documents.toArray(new DocumentEntity[]{}));
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                callback.onFinish(null);
            }
        }.execute();
    }

    public static void toggleFavorite(final Context context,
                                      final Set<DocumentMetadata> docs,
                                      final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                List<DocumentEntity> documents = new ArrayList<DocumentEntity>();
                for (DocumentMetadata doc : docs) {
                    DocumentEntity entity = AppDatabase.getInstance(context).documentDao().getDocument(doc.id);
                    entity.favorite = !doc.isFavorite;
                    documents.add(entity);
                }
                AppDatabase.getInstance(context).documentDao().update(documents.toArray(new DocumentEntity[]{}));
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                callback.onFinish(null);
            }
        }.execute();
    }

}
