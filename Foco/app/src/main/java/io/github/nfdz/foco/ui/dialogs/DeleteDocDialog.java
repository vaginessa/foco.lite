package io.github.nfdz.foco.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import io.github.nfdz.foco.R;

/**
 * This class eases to open a dialog asking a confirmation about the ongoing deletion to the user.
 */
public class DeleteDocDialog {

    /**
     * Callback to be implemented to receive the confirmation.
     */
    public interface Callback {
        void onDeleteConfirmed();
    }

    /**
     * Opens an AlertDialog with a message text asking user confirmation.
     * @param context
     * @param selectionSize
     * @param callback
     */
    public static void showDialog(Context context,
                                  int selectionSize,
                                  final Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_delete_title);
        if (selectionSize == 1) {
            builder.setMessage(R.string.dialog_delete_document_message);
        } else {
            builder.setMessage(R.string.dialog_delete_documents_message);
        }
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                callback.onDeleteConfirmed();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
