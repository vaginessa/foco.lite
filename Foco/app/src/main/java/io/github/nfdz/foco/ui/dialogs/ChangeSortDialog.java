package io.github.nfdz.foco.ui.dialogs;


import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.PreferencesUtils;

/**
 * This class eases to open a dialog to change de sort of the documents grid.
 */
public class ChangeSortDialog {

    /**
     * Callback to be implemented to be notified if the sort has changed.
     */
    public interface Callback {
        void onSortChanged();
    }

    /**
     * Opens a AlertDialog with a single choice list with all available sorts.
     * @param context
     * @param callback
     */
    public static void showDialog(final Context context,
                                  final Callback callback) {
        // get available sorts
        String titleLabel = context.getString(R.string.pref_sort_title_label);
        String editTimeLabel = context.getString(R.string.pref_sort_edit_time_label);
        String options[] = new String[] { titleLabel, editTimeLabel };
        final String titleKey = context.getString(R.string.pref_sort_title_key);
        final String editTimeKey = context.getString(R.string.pref_sort_edit_time_key);
        String selectedKey = PreferencesUtils.getPreferredSort(context);
        final int selected = selectedKey.equals(editTimeKey) ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_sort_title));
        builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int selection) {
                // if sort criteria has changed, saved it
                if (selection != selected) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... v) {
                            String sort = selection == 1 ? editTimeKey : titleKey;
                            PreferencesUtils.setPreferredSort(context, sort);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            callback.onSortChanged();
                        }
                    }.execute();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }
}