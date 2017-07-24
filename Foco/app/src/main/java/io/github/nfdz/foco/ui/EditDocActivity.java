package io.github.nfdz.foco.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import io.github.nfdz.foco.utils.FontChangeCrawler;
import timber.log.Timber;

public class EditDocActivity extends AppCompatActivity {

    public static final String EXTRA_DOC = "document";

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

        if (savedInstanceState == null) {
            loadTextAsync();
        } else {
            subscribeObserver();
            showContent();
        }

        mSelectionBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @OnClick(R.id.edit_selection_bar_format_bold)
    void asdf() {

    }

    @OnClick(R.id.edit_toolbar_back)
    void onBackClick() {
        // TODO ask if save
        if (!super.onNavigateUp()) {
            Timber.d("Cannot navigate up from edit activity");
            super.onBackPressed();
        }
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
                return true;
            case R.id.action_undo:
                return true;
            case R.id.action_redo:
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

    private void unsubscribeObserver() {
        mEditTextContent.setSelectionListener(null);
        mEditTextContent.removeTextChangedListener(mObserver);
    }

    private class TextObserver implements CustomEditText.SelectionListener, TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // nothing to do
        }
        @Override
        public void afterTextChanged(Editable s) {
            // nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Timber.d("#### onTextChanged "+start+","+before+","+count);
        }

        @Override
        public void onSelectionChanged(int selStart, int selEnd) {
            Timber.d("#### onSelectionChanged "+selStart+","+selEnd);
            if (selStart == selEnd && mSelectionBar.getVisibility() == View.VISIBLE) {
                mSelectionBar.setVisibility(View.INVISIBLE);
            } else if (selStart != selEnd && mSelectionBar.getVisibility() == View.INVISIBLE) {
                mSelectionBar.setVisibility(View.VISIBLE);
            }
        }
    }
}
