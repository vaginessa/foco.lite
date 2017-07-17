package io.github.nfdz.foco.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.nfdz.foco.R;

public class PreferencesUtils {

    /**
     * Retrieves sort preference.
     * @param context
     * @return String sort
     */
    public static String getPreferredSort(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForSort = context.getString(R.string.pref_sort_key);
        String defaultSort = context.getString(R.string.pref_sort_default);
        return sp.getString(keyForSort, defaultSort);
    }

    /**
     * Set sort preference.
     * @param context
     * @param sort
     * @return Returns true if the new value were successfully written
     * to persistent storage.
     */
    public static boolean setPreferredSort(Context context, String sort) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String keyForSort = context.getString(R.string.pref_sort_key);
        editor.putString(keyForSort, sort);
        return editor.commit();
    }
}
