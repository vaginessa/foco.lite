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
import io.github.nfdz.foco.model.Document;

/**
 * This class has static methods that ease perform common document operations
 * (create document, edit, delete, etc).
 */
public class TasksUtils {

    public static void createDocument(final Context context,
                                      final String name,
                                      final Callbacks.FinishCallback<DocumentMetadata> callback) {
        new AsyncTask<Void, Void, DocumentMetadata>() {
            @Override
            protected DocumentMetadata doInBackground(Void[] params) {
                DocumentEntity doc = new DocumentEntity();
                doc.name = name;
                long[] docId = AppDatabase.getInstance(context).documentDao().insert(doc);
                if (docId.length > 0 && docId[0] > -1) {
                    return AppDatabase.getInstance(context).documentDao().getDocumentMetadata(docId[0]);
                }
                return null;
            }
            @Override
            protected void onPostExecute(DocumentMetadata doc) {
                callback.onFinish(doc);
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

    public static void setTitle(final Context context,
                                final DocumentMetadata doc,
                                final String name,
                                final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                DocumentEntity entity = AppDatabase.getInstance(context).documentDao().getDocument(doc.id);
                entity.name = name;
                AppDatabase.getInstance(context).documentDao().update(entity);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                callback.onFinish(null);
            }
        }.execute();
    }


    public static void saveDocument(final Context context,
                                    final DocumentMetadata doc,
                                    final String text,
                                    final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                DocumentEntity entity = AppDatabase.getInstance(context).documentDao().getDocument(doc.id);
                entity.text = text;
                entity.lastEditionTime = System.currentTimeMillis();
                AppDatabase.getInstance(context).documentDao().update(entity);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                callback.onFinish(null);
            }
        }.execute();
    }

    public static void importDocument(final Context context,
                                      final Document document,
                                      final Callbacks.FinishCallback<DocumentMetadata> callback) {
        new AsyncTask<Void, Void, DocumentMetadata>() {
            @Override
            protected DocumentMetadata doInBackground(Void[] params) {
                DocumentEntity doc = new DocumentEntity(document);
                long[] docId = AppDatabase.getInstance(context).documentDao().insert(doc);
                if (docId.length > 0 && docId[0] > -1) {
                    return AppDatabase.getInstance(context).documentDao().getDocumentMetadata(docId[0]);
                }
                return null;
            }
            @Override
            protected void onPostExecute(DocumentMetadata doc) {
                callback.onFinish(doc);
            }
        }.execute();
    }
}
