package io.github.nfdz.foco.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import java.util.List;

import io.github.nfdz.foco.data.DatabaseManager;
import io.github.nfdz.foco.data.entity.DocumentMetadata;

/**
 * Document metadata list view model implementation.
 */
public class DocListViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        ABSENT.setValue(null);
    }

    private final LiveData<List<DocumentMetadata>> mObservableDocuments;

    /**
     * Default constructor.
     * @param application
     */
    public DocListViewModel(Application application) {
        super(application);

        // transform database created flag to live data.
        // if it is not created, it will return ABSENT field.
        // if it is created, it will return room live data.
        final DatabaseManager databaseManager = DatabaseManager.getInstance(application);
        LiveData<Boolean> databaseCreated = databaseManager.isDatabaseCreated();
        mObservableDocuments = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<DocumentMetadata>>>() {
                    @Override
                    public LiveData<List<DocumentMetadata>> apply(Boolean isDbCreated) {
                        if (!Boolean.TRUE.equals(isDbCreated)) {
                            return ABSENT;
                        } else {
                            return databaseManager.getDatabase().documentDao().loadAllDocumentsMetadata();
                        }
                    }
                });

        databaseManager.initDbAsync(this.getApplication());
    }

    public LiveData<List<DocumentMetadata>> getDocumentsMetadata() {
        return mObservableDocuments;
    }
}
