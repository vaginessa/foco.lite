package io.github.nfdz.foco.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.nfdz.foco.data.entity.DocumentEntity;
import timber.log.Timber;

public class DatabaseManager {

    private static DatabaseManager sInstance;

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private AppDatabase mDb;

    private final AtomicBoolean mInitializing = new AtomicBoolean(true);

    // singleton instantiation
    private static final Object LOCK = new Object();
    public synchronized static DatabaseManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DatabaseManager();
                }
            }
        }
        return sInstance;
    }

    /** used to observe when the database initialization is done */
    public LiveData<Boolean> isDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    @Nullable
    public AppDatabase getDatabase() {
        return mDb;
    }

    public void initDb(final Context context) {

        Timber.d("Creating DB from " + Thread.currentThread().getName());

        if (!mInitializing.compareAndSet(true, false)) {
            return;
        }

        mIsDatabaseCreated.setValue(false);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Context appContext = context.getApplicationContext();
                mDb = AppDatabase.getInstance(appContext);

                // insert dummy doc
                DocumentEntity doc = new DocumentEntity();
                doc.setLastEditionTimeMillis(System.currentTimeMillis());
                doc.setName("Testing Title");
                doc.setText("ASDFASFASDF ASDF ASDF ASDF AS FASF AS DF");
                doc.setWorkingTimeMillis(new Random().nextInt());
                mDb.documentDao().insert(doc);
                return null;
            }

            @Override
            protected void onPostExecute(Void ignored) {
                mIsDatabaseCreated.setValue(true);
            }
        }.execute();
    }
}