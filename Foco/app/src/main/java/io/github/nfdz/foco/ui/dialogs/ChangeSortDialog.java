package io.github.nfdz.foco.ui.dialogs;


import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.PreferencesUtils;

public class ChangeSortDialog {

    public interface Callback {
        void onSortChanged();
    }

    public static void showDialog(final Context context,
                                  final Callback callback) {

        String titleLabel = context.getString(R.string.pref_sort_title_label);
        String editTimeLabel = context.getString(R.string.pref_sort_edit_time_label);
        String wordsLabel = context.getString(R.string.pref_sort_words_label);
        String options[] = new String[] { titleLabel, editTimeLabel, wordsLabel };
        final String titleKey = context.getString(R.string.pref_sort_title_key);
        final String editTimeKey = context.getString(R.string.pref_sort_edit_time_key);
        final String wordsKey = context.getString(R.string.pref_sort_words_key);
        String selectedKey = PreferencesUtils.getPreferredSort(context);
        final int selected = selectedKey.equals(wordsKey) ? 2 : selectedKey.equals(editTimeKey) ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_sort_title));
        builder.setSingleChoiceItems(options, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int selection) {
                dialog.cancel();
                // if sort criteria has changed, saved it
                if (selection != selected) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... v) {
                            String sort = selection == 2 ? wordsKey :
                                    selection == 1 ? editTimeKey : titleKey;
                            PreferencesUtils.setPreferredSort(context, sort);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void aVoid) {
                            callback.onSortChanged();
                        }
                    }.execute();
                }
            }
        });
        builder.show();
    }
}