package io.github.nfdz.foco.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.AppDatabase;
import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Callbacks;
import io.github.nfdz.foco.ui.dialogs.AskSaveDialog;
import io.github.nfdz.foco.utils.FontChangeCrawler;
import io.github.nfdz.foco.utils.SelectionToolbarUtils;
import io.github.nfdz.foco.utils.TasksUtils;
import timber.log.Timber;

public class EditDocActivity extends AppCompatActivity {

    public static final String EXTRA_DOC = "document";

    private static final String TEXT_LOADED_KEY = "text-loaded";
    private static final String START_TIME_KEY = "start-time";
    private static final String TEXT_EDITED_KEY = "text-edited";

    public static void start(Context context, DocumentMetadata document) {
        Intent starter = new Intent(context, EditDocActivity.class);
        starter.putExtra(EXTRA_DOC, document);
        context.startActivity(starter);
    }

    @BindView(R.id.edit_toolbar) Toolbar mToolbar;
    @BindView(R.id.edit_content_text) CustomEditText mEditTextContent;
    @BindView(R.id.edit_toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.edit_loading) ProgressBar mLoading;
    @BindView(R.id.edit_selection_bar) View mSelectionBar;

    private DocumentMetadata mDocumentMetadata;
    private TextObserver mObserver = new TextObserver();
    private boolean mTextLoaded = false;
    private long mStartTime = -1;
    private boolean mTextEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // get document metadata
        mDocumentMetadata = getIntent().getParcelableExtra(EXTRA_DOC);
        if (mDocumentMetadata == null) {
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_edit_doc);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbarTitle.setText(mDocumentMetadata.getName());
        FontChangeCrawler fontChanger = new FontChangeCrawler(getAssets(),
                getString(R.string.font_libre_baskerville_regular));
        fontChanger.replaceFonts(mEditTextContent);

        // set up selection tool bar
        SelectionToolbarUtils.setDescriptionToToast(this,
                R.id.edit_selection_bar_format_bold,
                R.id.edit_selection_bar_format_italic,
                R.id.edit_selection_bar_format_strikethrough,
                R.id.edit_selection_bar_format_quote,
                R.id.edit_selection_bar_format_list_bulleted,
                R.id.edit_selection_bar_format_list_numbered,
                R.id.edit_selection_bar_format_link,
                R.id.edit_selection_bar_format_image,
                R.id.edit_selection_bar_format_video,
                R.id.edit_selection_bar_format_header,
                R.id.edit_selection_bar_format_header_2,
                R.id.edit_selection_bar_format_header_3);

        if (savedInstanceState == null || !savedInstanceState.getBoolean(TEXT_LOADED_KEY, false)) {
            loadTextAsync();
        } else {
            mStartTime = savedInstanceState.getLong(START_TIME_KEY, -1);
            mTextEdited = savedInstanceState.getBoolean(TEXT_EDITED_KEY, false);
            mTextLoaded = true;
            showContent();
            mEditTextContent.post(new Runnable() {
                @Override
                public void run() {
                    subscribeObserver();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TEXT_LOADED_KEY, mTextLoaded);
        outState.putLong(START_TIME_KEY, mStartTime);
        outState.putBoolean(TEXT_EDITED_KEY, mTextEdited);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStartTime < 0) mStartTime = System.currentTimeMillis();
    }

    @OnClick(R.id.edit_toolbar_back)
    void onBackClick() {
        // if text was edited ask save before
        if (mTextEdited) {
            AskSaveDialog.showDialog(this, new AskSaveDialog.Callback() {
                @Override
                public void onCloseWithoutSave() {
                    EditDocActivity.super.onNavigateUp();
                }
                @Override
                public void onSaveAndClose() {
                    showLoading();
                    saveDocument(new Callbacks.FinishCallback() {
                        @Override
                        public void onFinish(Object result) {
                            EditDocActivity.super.onNavigateUp();
                        }
                    });
                }
            });
        } else {
            super.onNavigateUp();
        }
    }

    @Override
    public void onBackPressed() {
        // if text was edited ask save before
        if (mTextEdited) {
            AskSaveDialog.showDialog(this, new AskSaveDialog.Callback() {
                @Override
                public void onCloseWithoutSave() {
                    EditDocActivity.super.onBackPressed();
                }
                @Override
                public void onSaveAndClose() {
                    showLoading();
                    saveDocument(new Callbacks.FinishCallback() {
                        @Override
                        public void onFinish(Object result) {
                            EditDocActivity.super.onNavigateUp();
                        }
                    });
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    private void saveDocument(final Callbacks.FinishCallback callback) {
        final Context context = this;
        long now = System.currentTimeMillis();
        long workingTime = mStartTime > 0 ? now - mStartTime : 0;
        TasksUtils.saveDocument(context,
                mDocumentMetadata,
                mEditTextContent.getText().toString(),
                workingTime,
                new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        mStartTime = System.currentTimeMillis();
                        mTextEdited = false;
                        callback.onFinish(result);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_music:
                return true;
            case R.id.action_preview:
                return true;
            case R.id.action_save:
                saveDocument(new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        Toast.makeText(EditDocActivity.this, R.string.save_success_msg, Toast.LENGTH_LONG).show();
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTextAsync() {
        showLoading();
        new AsyncTask<Void,Void,String>() {
            @Override
            protected String doInBackground(Void... params) {
                DocumentEntity entity = AppDatabase.getInstance(EditDocActivity.this)
                        .documentDao().getDocument(mDocumentMetadata.getId());
                return entity.text;
            }
            @Override
            protected void onPostExecute(String text) {
                mEditTextContent.setText(text);
                subscribeObserver();
                showContent();
                mTextLoaded = true;
            }
        }.execute();
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
        mEditTextContent.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mLoading.setVisibility(View.INVISIBLE);
        mEditTextContent.setVisibility(View.VISIBLE);
    }

    private void subscribeObserver() {
        mEditTextContent.setSelectionListener(mObserver);
        mEditTextContent.addTextChangedListener(mObserver);
    }

    private class TextObserver implements CustomEditText.SelectionListener, TextWatcher {

        @Override
        public void onSelectionChanged(int selStart, int selEnd) {
            if (selStart == selEnd && mSelectionBar.getVisibility() == View.VISIBLE) {
                mSelectionBar.setVisibility(View.INVISIBLE);
            } else if (selStart != selEnd && mSelectionBar.getVisibility() == View.INVISIBLE) {
                mSelectionBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (before > 0 || count > 0) {
                mTextEdited = true;
            }
            Timber.d("################## onTextChanged = "+start+" / "+before+" / "+count);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // nothing to do
        }
    }

    // text selection toolbar click handlers

    private void wrapSelection(String prefix, String suffix) {
        int selStart = mEditTextContent.getSelectionStart();
        int selEnd = mEditTextContent.getSelectionEnd();
        CharSequence selection = mEditTextContent.getText().subSequence(selStart, selEnd);
        CharSequence formattedSelection = prefix + selection + suffix;
        mEditTextContent.getText().replace(selStart, selEnd, formattedSelection);
    }

    @OnClick(R.id.edit_selection_bar_format_bold)
    public void onSelectionFormatBoldClick() {
        wrapSelection("**", "**");
    }

    @OnClick(R.id.edit_selection_bar_format_italic)
    public void onSelectionFormatItalicClick() {
        wrapSelection("_", "_");
    }

    @OnClick(R.id.edit_selection_bar_format_strikethrough)
    public void onSelectionFormatStrikethroughClick() {
        wrapSelection("~~", "~~");
    }

    private void addBeginningPerLineSelection(String mark) {
        int selStart = mEditTextContent.getSelectionStart();
        int selEnd = mEditTextContent.getSelectionEnd();
        CharSequence selection = mEditTextContent.getText().subSequence(selStart, selEnd);
        StringBuilder builder = new StringBuilder(mark);
        for (int i = 0; i < selection.length(); i++){
            char c = selection.charAt(i);
            if (c == '\n') {
                builder.append(c);
                builder.append(mark);
            } else {
                builder.append(c);
            }
        }
        String formattedSelection = builder.toString();
        mEditTextContent.getText().replace(selStart, selEnd, formattedSelection);
    }

    @OnClick(R.id.edit_selection_bar_format_quote)
    public void onSelectionFormatQuoteClick() {
        addBeginningPerLineSelection("> ");
    }

    @OnClick(R.id.edit_selection_bar_format_list_bulleted)
    public void onSelectionFormatListBulletedClick() {
        addBeginningPerLineSelection("- ");
    }

    @OnClick(R.id.edit_selection_bar_format_list_numbered)
    public void onSelectionFormatListNumberedClick() {
        int selStart = mEditTextContent.getSelectionStart();
        int selEnd = mEditTextContent.getSelectionEnd();
        CharSequence selection = mEditTextContent.getText().subSequence(selStart, selEnd);
        int listIndex = 1;
        StringBuilder builder = new StringBuilder(listIndex + ". ");
        for (int i = 0; i < selection.length(); i++){
            char c = selection.charAt(i);
            if (c == '\n') {
                listIndex++;
                builder.append(c);
                builder.append(listIndex);
                builder.append(". ");
            } else {
                builder.append(c);
            }
        }
        String formattedSelection = builder.toString();
        mEditTextContent.getText().replace(selStart, selEnd, formattedSelection);
    }

    @OnClick(R.id.edit_selection_bar_format_link)
    public void onSelectionFormatLinkClick() {
        wrapSelection("[", "](http://)");
    }

    @OnClick(R.id.edit_selection_bar_format_image)
    public void onSelectionFormatImageClick() {
        wrapSelection("![", "](http://)");
    }

    @OnClick(R.id.edit_selection_bar_format_video)
    public void onSelectionFormatVideoClick() {
        wrapSelection("@[youtube](", ")");
    }

    private void addBeginningSelection(String mark) {
        int selStart = mEditTextContent.getSelectionStart();
        int selEnd = mEditTextContent.getSelectionEnd();
        CharSequence selection = mEditTextContent.getText().subSequence(selStart, selEnd);
        CharSequence formattedSelection = mark + selection;
        mEditTextContent.getText().replace(selStart, selEnd, formattedSelection);
    }

    @OnClick(R.id.edit_selection_bar_format_header)
    public void onSelectionFormatHeaderClick() {
        addBeginningSelection("# ");
    }

    @OnClick(R.id.edit_selection_bar_format_header_2)
    public void onSelectionFormatHeader2Click() {
        addBeginningSelection("## ");
    }

    @OnClick(R.id.edit_selection_bar_format_header_3)
    public void onSelectionFormatHeader3Click() {
        addBeginningSelection("### ");
    }
}