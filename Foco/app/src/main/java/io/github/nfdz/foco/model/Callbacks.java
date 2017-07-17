package io.github.nfdz.foco.model;

/**
 * This is a useful callback mechanisms so we can abstract our async tasks.
 */
public class Callbacks {

    public interface OperationCallback<T> {
        void onSuccess(T result);
        void onError(String msg, Throwable th);
    }

    public interface FinishCallback<T> {
        void onFinish(T result);
    }

}