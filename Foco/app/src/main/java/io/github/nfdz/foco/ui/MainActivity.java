package io.github.nfdz.foco.ui;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.PreferencesUtils;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Callbacks;
import io.github.nfdz.foco.model.DocumentLastEditionComparator;
import io.github.nfdz.foco.model.DocumentNameComparator;
import io.github.nfdz.foco.model.DocumentWordsComparator;
import io.github.nfdz.foco.ui.dialogs.ChangeSortDialog;
import io.github.nfdz.foco.ui.dialogs.CreateDocDialog;
import io.github.nfdz.foco.ui.dialogs.DeleteDocDialog;
import io.github.nfdz.foco.ui.dialogs.EditDocTitleDialog;
import io.github.nfdz.foco.ui.dialogs.SearchTextDialog;
import io.github.nfdz.foco.utils.ImportExportUtils;
import io.github.nfdz.foco.utils.SelectionToolbarUtils;
import io.github.nfdz.foco.utils.TasksUtils;
import io.github.nfdz.foco.viewmodel.DocListViewModel;

/**
 * Main activity implementation. It has recycler grid view and provides several document actions
 * (edit title, cover, mark as favorite, delete, etc).
 */
public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        DocsAdapter.DocsClickHandler, LifecycleRegistryOwner {

    private static final String SELECTED_DOCUMENTS_KEY = "selected-documents";
    private static final String SEARCH_TEXT_KEY = "search-text-document";

    private static final float PERCENTAGE_LOGO_THRESHOLD = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    private final Set<DocumentMetadata> mSelectedDocuments = new HashSet<>();

    private boolean mToolbarLogoVisible = false;
    private boolean mLayoutLogoVisible = true;
    private boolean mExportEnabled;

    private DocsAdapter mAdapter;

    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.main_fab_add) FloatingActionButton mFab;
    @BindView(R.id.main_toolbar_image_logo) ImageView mToolbarLogo;
    @BindView(R.id.main_layout_image_logo) ImageView mLayoutLogo;
    @BindView(R.id.main_app_bar) AppBarLayout mAppBar;
    @BindView(R.id.main_rv_docs) RecyclerView mRecyclerView;
    @BindView(R.id.main_loading) ProgressBar mLoading;
    @BindView(R.id.main_collapsing_layout) CollapsingToolbarLayout mCollapsingLayout;

    @BindView(R.id.main_selection_bar) LinearLayout mSelectionBar;
    @BindView(R.id.main_selection_bar_edit_title) ImageButton mEditTitleSelectionBar;
    @BindView(R.id.main_selection_bar_export) ImageButton mExportSelectionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        // set up selection tool bar
        SelectionToolbarUtils.setDescriptionToToast(this,
                R.id.main_selection_bar_exit,
                R.id.main_selection_bar_delete,
                R.id.main_selection_bar_export,
                R.id.main_selection_bar_favorite,
                R.id.main_selection_bar_edit_title);

        // disable export feature if SDK version is not valid
        mExportEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (!mExportEnabled) mExportSelectionBar.setVisibility(View.GONE);

        // set up recycler view
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DocsAdapter(this, mSelectedDocuments, this);
        mRecyclerView.setAdapter(mAdapter);
        updateAdapterComparator();

        mAppBar.addOnOffsetChangedListener(this);
        startAlphaAnimation(mToolbarLogo, 0, View.INVISIBLE);

        showLoading();
        // subscribe to view model
        DocListViewModel viewModel = ViewModelProviders.of(this).get(DocListViewModel.class);
        subscribeUi(viewModel);
    }

    /**
     * Subscribes this activity to LiveData view model.
     * @param viewModel
     */
    private void subscribeUi(DocListViewModel viewModel) {
        viewModel.getDocumentsMetadata().observe(this, new Observer<List<DocumentMetadata>>() {
            @Override
            public void onChanged(@Nullable List<DocumentMetadata> docs) {
                if (docs != null) {
                    showData();
                    mAdapter.setDocumentList(docs);
                } else {
                    showLoading();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedDocuments.size() > 0) {
            outState.putParcelableArrayList(SELECTED_DOCUMENTS_KEY,
                    new ArrayList<>(mSelectedDocuments));
        }
        if (mAdapter.hasFilter()) {
            outState.putString(SEARCH_TEXT_KEY, mAdapter.getFilterText());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAppBar.setExpanded(false, false);
        if (savedInstanceState.containsKey(SELECTED_DOCUMENTS_KEY)) {
            ArrayList<DocumentMetadata> docs = savedInstanceState.getParcelableArrayList(SELECTED_DOCUMENTS_KEY);
            mSelectedDocuments.addAll(docs);
            mAdapter.refreshSelectedDocuments();
            updateSelectionBar();
        }
        if (savedInstanceState.containsKey(SEARCH_TEXT_KEY)) {
            String filterText = savedInstanceState.getString(SEARCH_TEXT_KEY);
            if (!TextUtils.isEmpty(filterText)) {
                mAdapter.setFilter(filterText);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mSelectedDocuments.size() != 0) {
            onSelectionExitClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (mAdapter.hasFilter()) {
            item.setIcon(R.drawable.ic_search_cancel);
            item.setTitle(R.string.action_search_cancel);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            ChangeSortDialog.showDialog(this, new ChangeSortDialog.Callback() {
                @Override
                public void onSortChanged() {
                    updateAdapterComparator();
                }
            });
            return true;
        } else if (id == R.id.action_search) {
            if (mAdapter.hasFilter()) {
                mAdapter.setFilter(null);
                item.setIcon(R.drawable.ic_search);
                item.setTitle(R.string.action_search);
            } else {
                SearchTextDialog.showDialog(this, new SearchTextDialog.Callback() {
                    @Override
                    public void onSearch(String text) {
                        mAdapter.setFilter(text);
                    }
                    @Override
                    public void onSearchTextChanged(String text) {
                        boolean noFilter = TextUtils.isEmpty(text);
                        if (!noFilter && !mAdapter.hasFilter()) {
                            item.setIcon(R.drawable.ic_search_cancel);
                            item.setTitle(R.string.action_search_cancel);
                        } else if (noFilter && mAdapter.hasFilter()) {
                            item.setIcon(R.drawable.ic_search);
                            item.setTitle(R.string.action_search);
                        }
                        mAdapter.setFilter(text);
                    }
                    @Override
                    public void onSearchCancel() {
                        mAdapter.setFilter(null);
                        item.setIcon(R.drawable.ic_search);
                        item.setTitle(R.string.action_search);
                    }
                });
            }
            return true;
        } else if (id == R.id.action_import) {
            // assume that this code will be only reached if sdk >= 19
            ImportExportUtils.importBookmarks(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (ImportExportUtils.onImportActivityResult(requestCode, resultCode, resultData, this)) return;
        if (mSelectedDocuments.size() == 1) {
            DocumentMetadata doc = mSelectedDocuments.iterator().next();
            if (ImportExportUtils.onExportActivityResult(requestCode, resultCode, resultData, this, doc)) return;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void updateAdapterComparator() {
        String sort = PreferencesUtils.getPreferredSort(this);
        if (getString(R.string.pref_sort_words_key).equals(sort)) {
            mAdapter.setComparator(new DocumentWordsComparator());
        } else if (getString(R.string.pref_sort_edit_time_key).equals(sort)) {
            mAdapter.setComparator(new DocumentLastEditionComparator());
        } else {
            mAdapter.setComparator(new DocumentNameComparator());
        }
    }

    private void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        // update logo
        if (percentage < PERCENTAGE_LOGO_THRESHOLD) {
            if (mToolbarLogoVisible) {
                startAlphaAnimation(mToolbarLogo, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mToolbarLogoVisible = false;
            }
            if (!mLayoutLogoVisible) {
                startAlphaAnimation(mLayoutLogo, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mLayoutLogoVisible = true;
            }
        } else {
            if (!mToolbarLogoVisible) {
                startAlphaAnimation(mToolbarLogo, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mToolbarLogoVisible = true;
            }
            if (mLayoutLogoVisible) {
                startAlphaAnimation(mLayoutLogo, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mLayoutLogoVisible = false;
            }
        }

        // It is necessary to make sure that the toolbar is in the front because in some
        // versions of android (not all :S) the collapsible layout is placed above the toolbar
        // when it is expanding
        mToolbar.bringToFront();
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    private void showData() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.main_fab_add)
    void onCreateDocumentClick() {
        CreateDocDialog.showDialog(this, new CreateDocDialog.Callback() {
            @Override
            public void onCreateDocument(String name) {
                TasksUtils.createDocument(MainActivity.this, name, new Callbacks.FinishCallback<DocumentMetadata>() {
                    @Override
                    public void onFinish(DocumentMetadata doc) {
                        if (doc != null) {
                            openDocument(doc);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDocumentClick(DocumentMetadata doc) {
        openDocument(doc);
    }

    private void openDocument(DocumentMetadata doc) {
        mSelectedDocuments.clear();
        mAdapter.refreshSelectedDocuments();
        showNoSelectionMode();
        EditDocActivity.start(this, doc);
    }

    @Override
    public void onDocumentLongClick(DocumentMetadata doc) {
        if (mSelectedDocuments.contains(doc)) {
            mSelectedDocuments.remove(doc);
        } else {
            mSelectedDocuments.add(doc);
        }
        mAdapter.refreshSelectedDocuments();
        updateSelectionBar();
    }

    private void updateSelectionBar() {
        switch (mSelectedDocuments.size()) {
            case 0:
                showNoSelectionMode();
                break;
            case 1:
                showSingleSelectionMode();
                break;
            default:
                showMultipleSelectionMode();
        }
    }

    private void showNoSelectionMode() {
        mSelectionBar.setVisibility(View.INVISIBLE);
    }

    private void showSingleSelectionMode() {
        mEditTitleSelectionBar.setVisibility(View.VISIBLE);
        if (mExportEnabled) mExportSelectionBar.setVisibility(View.VISIBLE);
        mSelectionBar.setVisibility(View.VISIBLE);
    }

    private void showMultipleSelectionMode() {
        mEditTitleSelectionBar.setVisibility(View.GONE);
        if (mExportEnabled) mExportSelectionBar.setVisibility(View.GONE);
        mSelectionBar.setVisibility(View.VISIBLE);
    }

    /**
     * This listener is needed in order to avoid that documents that are below this layout could
     * be clicked accidentally.
     */
    @OnClick(R.id.main_selection_bar)
    public void onSelectionBarClick() {
        // nothing to do
    }

    @OnClick(R.id.main_selection_bar_exit)
    public void onSelectionExitClick() {
        mSelectedDocuments.clear();
        mAdapter.refreshSelectedDocuments();
        showNoSelectionMode();
    }

    @OnClick(R.id.main_selection_bar_delete)
    public void onSelectionDeleteClick() {
        DeleteDocDialog.showDialog(this,
                mSelectedDocuments.size(),
                new DeleteDocDialog.Callback() {
                    @Override
                    public void onDeleteConfirmed() {
                        TasksUtils.deleteDocument(MainActivity.this,
                                mSelectedDocuments,
                                new Callbacks.FinishCallback<Void>() {
                                    @Override
                                    public void onFinish(Void result) {
                                        mSelectedDocuments.clear();
                                        showNoSelectionMode();
                                    }
                                });
                    }
                });
    }

    @OnClick(R.id.main_selection_bar_export)
    public void onSelectionExportClick() {
        DocumentMetadata doc = mSelectedDocuments.iterator().next();
        ImportExportUtils.exportDocument(this, doc);
    }

    @OnClick(R.id.main_selection_bar_favorite)
    public void onSelectionFavoriteClick() {
        TasksUtils.toggleFavorite(this,
                mSelectedDocuments,
                new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        mSelectedDocuments.clear();
                        showNoSelectionMode();
                        mAdapter.refreshSelectedDocuments();
                    }
                });
    }

    @OnClick(R.id.main_selection_bar_edit_title)
    public void onSelectionEditTitleClick() {
        final DocumentMetadata doc = mSelectedDocuments.iterator().next();
        EditDocTitleDialog.showDialog(this, doc, new EditDocTitleDialog.Callback() {
            @Override
            public void onTitleChanged(String name) {
                TasksUtils.setTitle(MainActivity.this, doc, name, new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        mSelectedDocuments.clear();
                        showNoSelectionMode();
                        mAdapter.refreshSelectedDocuments();
                    }
                });
            }
        });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }

}
