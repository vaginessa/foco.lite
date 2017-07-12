package io.github.nfdz.foco.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

import static io.github.nfdz.foco.data.AppDatabase.DATABASE_NAME;

public class DatabaseCreator {

    private static DatabaseCreator sInstance;

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private AppDatabase mDb;

    private final AtomicBoolean mInitializing = new AtomicBoolean(true);

    // singleton instantiation
    private static final Object LOCK = new Object();
    public synchronized static DatabaseCreator getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DatabaseCreator();
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

    public void createDb(final Context context) {

        Timber.d("Creating DB from " + Thread.currentThread().getName());

        if (!mInitializing.compareAndSet(true, false)) {
            return;
        }

        mIsDatabaseCreated.setValue(false);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Timber.d("Starting bg job " + Thread.currentThread().getName());

                Context appContext = context.getApplicationContext();

                appContext.deleteDatabase(DATABASE_NAME);

                AppDatabase db = Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME).build();

                addDelay();

                Timber.d("DB was created in thread " + Thread.currentThread().getName());

                mDb = db;
                return null;
            }

            @Override
            protected void onPostExecute(Void ignored) {
                mIsDatabaseCreated.setValue(true);
            }
        }.execute();
    }

    private void addDelay() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {}
    }
}