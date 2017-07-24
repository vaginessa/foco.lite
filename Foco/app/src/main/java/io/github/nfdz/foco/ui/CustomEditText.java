package io.github.nfdz.foco.ui;

import android.content.Context;
import android.util.AttributeSet;

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    public interface SelectionListener {
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
