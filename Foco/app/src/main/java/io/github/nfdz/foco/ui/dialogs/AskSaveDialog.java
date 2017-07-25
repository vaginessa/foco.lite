package io.github.nfdz.foco.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import io.github.nfdz.foco.R;

public class AskSaveDialog {

    /**
     * Callback to be implemented to receive the confirmation.
     */
    public interface Callback {
        void onCloseWithoutSave();
        void onSaveAndClose();
    }

    /**
     * Opens an AlertDialog with a message text asking user if he wants to save.
     * @param context
     * @param callback
     */
    public static void showDialog(Context context,
                                  final Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_ask_save_title);
        builder.setMessage(R.string.dialog_ask_save_message);

        builder.setPositiveButton(R.string.dialog_ask_save_save_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                callback.onSaveAndClose();
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.dialog_ask_save_close_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onCloseWithoutSave();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
