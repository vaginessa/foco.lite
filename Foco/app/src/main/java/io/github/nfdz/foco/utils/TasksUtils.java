package io.github.nfdz.foco.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.nfdz.foco.data.AppDatabase;
import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Callbacks;
import io.github.nfdz.foco.model.Document;

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
                    if (!TextUtils.isEmpty(doc.coverImage)) {
                        File file = new File(doc.coverImage);
                        file.delete();
                    }
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

    public static void setCoverColor(final Context context,
                                     final DocumentMetadata doc,
                                     final int color,
                                     final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                DocumentEntity entity = AppDatabase.getInstance(context).documentDao().getDocument(doc.id);
                entity.coverColor = color;
                if (!TextUtils.isEmpty(entity.coverImage)) {
                    File file = new File(entity.coverImage);
                    file.delete();
                    entity.coverImage = Document.NULL_COVER_IMAGE;
                }
                AppDatabase.getInstance(context).documentDao().update(entity);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                callback.onFinish(null);
            }
        }.execute();
    }

    public static void setCoverImage(final Context context,
                                     final DocumentMetadata doc,
                                     final String imagePath,
                                     final Callbacks.FinishCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                DocumentEntity entity = AppDatabase.getInstance(context).documentDao().getDocument(doc.id);
                if (!TextUtils.isEmpty(entity.coverImage)) {
                    File file = new File(entity.coverImage);
                    file.delete();
                }
                entity.coverImage = imagePath;
                entity.coverColor = Document.NULL_COVER_COLOR;
                AppDatabase.getInstance(context).documentDao().update(entity);
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

}
