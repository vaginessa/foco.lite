package io.github.nfdz.foco.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

import io.github.nfdz.foco.R;
import timber.log.Timber;

public class DocItemUtils {

    private static final int TITLE_SIZE_LARGE_TO_MEDIUM_THRESHOLD_CHARS = 22;
    private static final int TITLE_SIZE_MEDIUM_TO_SMALL_THRESHOLD_CHARS = 40;

    public static void resolveTitleSize(Context context, String title, TextView titleView) {
        if (TextUtils.isEmpty(title) || title.length() <= TITLE_SIZE_LARGE_TO_MEDIUM_THRESHOLD_CHARS) {
            float size = context.getResources().getDimension(R.dimen.doc_item_title_large) /
                    context.getResources().getDisplayMetrics().density;
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        } else if (title.length() <= TITLE_SIZE_MEDIUM_TO_SMALL_THRESHOLD_CHARS) {
            float size = context.getResources().getDimension(R.dimen.doc_item_title_medium) /
                    context.getResources().getDisplayMetrics().density;
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        } else {
            float size = context.getResources().getDimension(R.dimen.doc_item_title_small) /
                    context.getResources().getDisplayMetrics().density;
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        }
    }
}
