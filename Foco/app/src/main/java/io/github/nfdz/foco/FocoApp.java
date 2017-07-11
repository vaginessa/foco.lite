package io.github.nfdz.foco;

import android.app.Application;

import timber.log.Timber;

public class FocoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}