package io.github.nfdz.focolite;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import io.github.nfdz.focolite.data.AppDatabase;
import io.github.nfdz.focolite.data.PreferencesUtils;
import io.github.nfdz.focolite.data.entity.DocumentEntity;
import timber.log.Timber;

public class FocoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
        // insert sample document if it has never done before (first execution)
        if (!PreferencesUtils.getInsertedSampleFlag(this)) {
            insertSampleDocument(this);
        }
    }

    /**
     * This method inserts sample document in database.
     * @param context
     */
    private static void insertSampleDocument(final Context context) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DocumentEntity doc = new DocumentEntity();
                doc.name = context.getString(R.string.sample_doc_title);
                doc.text = context.getString(R.string.sample_doc_text);
                long[] docId = AppDatabase.getInstance(context).documentDao().insert(doc);
                if (docId.length == 0 || docId[0] <= -1) {
                    Timber.e("There was an error inserting sample document");
                } else {
                    PreferencesUtils.setInsertedSampleFlag(context);
                }
                return null;
            }
        }.execute();
    }
}