package io.github.nfdz.focolite.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public void initDbAsync(final Context context) {

        if (!mInitializing.compareAndSet(true, false)) {
            return;
        }

        mIsDatabaseCreated.setValue(false);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Context appContext = context.getApplicationContext();
                mDb = AppDatabase.getInstance(appContext);
                return null;
            }

            @Override
            protected void onPostExecute(Void ignored) {
                mIsDatabaseCreated.setValue(true);
            }
        }.execute();
    }
}