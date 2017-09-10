package io.github.nfdz.focolite.model;

/**
 * This is a useful callback mechanisms so we can abstract our async tasks.
 */
public class Callbacks {

    /**
     * This callback interface has got two methods and it ensures that one and only one
     * of its methods will be called.
     * @param <T>
     */
    public interface OperationCallback<T> {
        /**
         * This method is called if the operation finishes successfully.
         * @param result
         */
        void onSuccess(T result);

        /**
         * This method is called if the operation finishes incorrectly or has got errors
         * during the operation.
         * @param msg
         * @param th
         */
        void onError(String msg, Throwable th);
    }

    /**
     * This callback interface has got only one method and it ensures that it will be called.
     * @param <T>
     */
    public interface FinishCallback<T> {
        void onFinish(T result);
    }

}