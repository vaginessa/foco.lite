package io.github.nfdz.foco.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import io.github.nfdz.foco.lite.R;
import io.github.nfdz.foco.data.AppDatabase;
import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Callbacks;
import io.github.nfdz.foco.model.Document;
import io.github.nfdz.foco.model.DocumentSerializer;
import io.github.nfdz.foco.model.SerializationException;
import timber.log.Timber;

/**
 * This class has static methods to manage import/export operations.
 */
public class ImportExportUtils {

    private static final int READ_REQUEST_CODE = 921;
    private static final int WRITE_REQUEST_CODE = 886;

    private static final String MIME_TYPE = "text/*";
    private static final String SUGGESTED_NAME_FORMAT = "%s-%s.foco";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * This method starts open document system activity.
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void importBookmarks(Activity activity) {
        // choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // filter to show only plain text
        intent.setType(MIME_TYPE);

        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * This methods manage the result of an open document activity.
     * @param requestCode
     * @param resultCode
     * @param resultData
     * @param context
     * @return true if activity result was managed by this method, false if not.
     */
    public static boolean onImportActivityResult(int requestCode,
                                                 int resultCode,
                                                 Intent resultData,
                                                 final Context context) {
        if (requestCode == READ_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                new AsyncTask<Void, Void, Void>(){
                    private Document document;
                    private String error;
                    private String warning;
                    @Override
                    protected Void doInBackground(Void... params) {
                        InputStream in = null;
                        try {
                            DocumentFile file = DocumentFile.fromSingleUri(context, uri);
                            in = context.getContentResolver().openInputStream(file.getUri());
                            if (in.available() != 0) {
                                String inputStreamString = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
                                try {
                                    document = DocumentSerializer.deserializeDocument(inputStreamString);
                                } catch (SerializationException e) {
                                    DocumentSerializer.DocumentImpl documentImpl = new DocumentSerializer.DocumentImpl();
                                    documentImpl.text = inputStreamString;
                                    String name = file.getName();
                                    if (TextUtils.isEmpty(name)) {
                                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                                        String currentDate = sdf.format(new Date());
                                        documentImpl.name = currentDate;
                                    } else {
                                        documentImpl.name = name;
                                    }
                                    document = documentImpl;
                                    warning = context.getString(R.string.import_warning_metadata);
                                }
                            } else {
                                error = context.getString(R.string.import_error_empty);
                            }
                        } catch (IOException e) {
                            Timber.e(e, "Error reading document");
                            error = context.getString(R.string.import_error_reading);
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    // swallow
                                }
                            }
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void v) {
                        if (document != null) {
                            TasksUtils.importDocument(context,
                                    document,
                                    new Callbacks.FinishCallback<DocumentMetadata>() {
                                @Override
                                public void onFinish(DocumentMetadata result) {
                                    if (result != null) {
                                        if (warning != null) {
                                            Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, R.string.import_success, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        String msg = context.getString(R.string.import_error_format,
                                                context.getString(R.string.import_error_db));
                                        Toast.makeText(context,
                                                msg,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(context,
                                    context.getString(R.string.import_error_format, error),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(context, R.string.file_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method starts create document system activity.
     * @param activity
     * @param doc
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void exportDocument(Activity activity,
                                      DocumentMetadata doc) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // show only results that can be "opened", such as a file
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // create a file with plain text MIME type
        intent.setType(MIME_TYPE);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String currentDate = sdf.format(new Date());
        String suggestedName = String.format(SUGGESTED_NAME_FORMAT, doc.getName(), currentDate);
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * This methods manage the result of an create document activity.
     * @param requestCode
     * @param resultCode
     * @param resultData
     * @param context
     * @param docMetadata
     * @return true if activity result was managed by this method, false if not.
     */
    public static boolean onExportActivityResult(int requestCode,
                                                 int resultCode,
                                                 Intent resultData,
                                                 final Context context,
                                                 final DocumentMetadata docMetadata) {
        if (requestCode == WRITE_REQUEST_CODE) {
            // URI to user document is contained in the return intent
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.getData() != null) {
                final Uri uri = resultData.getData();
                new AsyncTask<Void,Void,Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        DocumentEntity entity = AppDatabase.getInstance(context)
                                .documentDao().getDocument(docMetadata.getId());
                        String serializedDoc = DocumentSerializer.serializeDocument(entity);
                        OutputStream out = null;
                        try {
                            DocumentFile newFile = DocumentFile.fromSingleUri(context, uri);
                            out = context.getContentResolver().openOutputStream(newFile.getUri());
                            out.write(serializedDoc.getBytes("UTF-8"));
                            return true;
                        } catch (IOException e) {
                            Timber.d(e, "There was an error writing file where to export document");
                            return false;
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    // swallow
                                }
                            }
                        }
                    }
                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (Boolean.TRUE.equals(result)) {
                            Toast.makeText(context, R.string.export_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, R.string.export_error, Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            } else if (resultCode != Activity.RESULT_CANCELED) {
                Toast.makeText(context, R.string.file_error, Toast.LENGTH_LONG).show();
            }
            return true;
        } else {
            return false;
        }
    }
}
