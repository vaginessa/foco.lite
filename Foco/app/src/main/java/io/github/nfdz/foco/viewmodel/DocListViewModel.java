package io.github.nfdz.foco.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import java.util.List;

import io.github.nfdz.foco.data.DatabaseCreator;
import io.github.nfdz.foco.data.entity.DocumentMetadata;

public class DocListViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        ABSENT.setValue(null);
    }

    private final LiveData<List<DocumentMetadata>> mObservableDocuments;

    public DocListViewModel(Application application) {
        super(application);

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(application);

        LiveData<Boolean> databaseCreated = databaseCreator.isDatabaseCreated();
        mObservableDocuments = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<DocumentMetadata>>>() {
                    @Override
                    public LiveData<List<DocumentMetadata>> apply(Boolean isDbCreated) {
                        if (!Boolean.TRUE.equals(isDbCreated)) {
                            return ABSENT;
                        } else {
                            return databaseCreator.getDatabase().documentDao().loadAllDocumentsMetadata();
                        }
                    }
                });

        databaseCreator.createDb(this.getApplication());
    }

    public LiveData<List<DocumentMetadata>> getDocumentsMetadata() {
        return mObservableDocuments;
    }
}
