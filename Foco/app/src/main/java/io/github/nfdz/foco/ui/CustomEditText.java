package io.github.nfdz.foco.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * This EditText implementation supports all the functionality of its parent and
 * adds the feature of listening when the selection has changed.
 */
public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    /**
     * This interface must be implemented to listen selection changes.
     */
    public interface SelectionListener {
        /**
         * This method is called when the selection has changed. Cursor position changes
         * is notified also (when selStart is equal to selEnd).
         *
         * @param selStart The new selection start location.
         * @param selEnd The new selection end location.
         */
        void onSelectionChanged(int selStart, int selEnd);
    }

    private SelectionListener mListener;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectionListener(SelectionListener listener) {
        mListener = listener;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (mListener != null) mListener.onSelectionChanged(selStart, selEnd);
        super.onSelectionChanged(selStart, selEnd);
    }
}
