package io.github.nfdz.foco.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicReference;

import io.github.nfdz.foco.lite.R;

/**
 * This class eases to open a dialog with an edit text input to get search text.
 */
public class SearchTextDialog {
    /**
     * Callback to be implemented to receive the search text.
     */
    public interface Callback {
        void onSearch(String text);
        void onSearchTextChanged(String text);
        void onSearchCancel();
    }

    /**
     * Opens an AlertDialog with an input text.
     * @param context
     * @param callback
     */
    public static void showDialog(Context context,
                                  final Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_search_doc_title);
        builder.setView(R.layout.dialog_search_doc);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onSearchCancel();
                dialog.dismiss();
            }
        });

        // ok button callback
        final AtomicReference<String> textRef = new AtomicReference<>();
        builder.setPositiveButton(R.string.dialog_search_doc_ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String text = textRef.get();
                if (!TextUtils.isEmpty(text)) {
                    callback.onSearch(text);
                    dialog.dismiss();
                }
            }
        });

        // create dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // ensure that ok button will only be available if input text is not empty
        // and notify about text changes to callback
        final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        EditText textInput = (EditText) dialog.findViewById(R.id.dialog_search_doc_text);
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textRef.set(s.toString());
                callback.onSearchTextChanged(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                positiveButton.setEnabled(s.length() > 0);
            }
        });
    }
}
