package io.github.nfdz.focolite.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

/**
 * This class has static methods with common functionalities of any kind of selection toolbars.
 */
public class SelectionToolbarUtils {

    public static void setDescriptionToToast(Activity activity, @IdRes int... viewIds) {
        SelectionBarLongClickHandler handler = new SelectionBarLongClickHandler(activity);
        for (int viewId : viewIds) {
            View view = activity.findViewById(viewId);
            if (view != null) view.setOnLongClickListener(handler);
        }
    }

    public static class SelectionBarLongClickHandler implements View.OnLongClickListener {

        private final Context mContext;

        public SelectionBarLongClickHandler(Context context) {
            mContext = context;
        }
        @Override
        public boolean onLongClick(View v) {
            CharSequence description = v.getContentDescription();
            if (!TextUtils.isEmpty(description)) {
                Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }
}
