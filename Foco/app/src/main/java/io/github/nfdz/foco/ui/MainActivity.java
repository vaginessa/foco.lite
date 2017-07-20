package io.github.nfdz.foco.ui;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import io.github.nfdz.foco.ui.dialogs.EditDocDialog;
import io.github.nfdz.foco.utils.TasksUtils;
import io.github.nfdz.foco.viewmodel.DocListViewModel;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        DocsAdapter.DocsClickHandler, LifecycleRegistryOwner {

    private static final String SELECTED_DOCUMENTS_KEY = "selected-documents";

    private static final float PERCENTAGE_ADD_DOC_THRESHOLD = 0.7f;
    private static final float PERCENTAGE_LOGO_THRESHOLD = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    private final Set<DocumentMetadata> mSelectedDocuments = new HashSet<>();

    private boolean mToolbarLogoVisible = false;
    private boolean mLayoutLogoVisible = true;

    private DocsAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.main_fab_add) FloatingActionButton mFab;
    @BindView(R.id.main_toolbar_image_logo) ImageView mToolbarLogo;
    @BindView(R.id.main_layout_image_logo) ImageView mLayoutLogo;
    @BindView(R.id.main_app_bar) AppBarLayout mAppBar;
    @BindView(R.id.main_rv_docs) RecyclerView mRecyclerView;
    @BindView(R.id.main_loading) ProgressBar mLoading;
    @BindView(R.id.main_collapsing_layout) CollapsingToolbarLayout mCollapsingLayout;

    @BindView(R.id.main_selection_bar) LinearLayout mSelectionBar;
    @BindView(R.id.main_selection_bar_exit) ImageButton mExitSelectionBar;
    @BindView(R.id.main_selection_bar_delete) ImageButton mDeleteSelectionBar;
    @BindView(R.id.main_selection_bar_cloud) ImageButton mCloudSelectionBar;
    @BindView(R.id.main_selection_bar_share) ImageButton mShareSelectionBar;
    @BindView(R.id.main_selection_bar_favorite) ImageButton mFavoriteSelectionBar;
    @BindView(R.id.main_selection_bar_edit) ImageButton mEditSelectionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        // set up selection tool bar
        SelectionBarLongClickHandler handler = new SelectionBarLongClickHandler();
        mExitSelectionBar.setOnLongClickListener(handler);
        mDeleteSelectionBar.setOnLongClickListener(handler);
        mCloudSelectionBar.setOnLongClickListener(handler);
        mShareSelectionBar.setOnLongClickListener(handler);
        mFavoriteSelectionBar.setOnLongClickListener(handler);
        mEditSelectionBar.setOnLongClickListener(handler);

        // set up recycler view
        int spanCount = getResources().getInteger(R.integer.grid_doc_columns);
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        mLayoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DocsAdapter(this, mSelectedDocuments, this);
        mRecyclerView.setAdapter(mAdapter);
        updateAdapterComparator();

        mAppBar.addOnOffsetChangedListener(this);
        startAlphaAnimation(mToolbarLogo, 0, View.INVISIBLE);

        showLoading();
        DocListViewModel viewModel = ViewModelProviders.of(this).get(DocListViewModel.class);
        subscribeUi(viewModel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedDocuments.size() > 0) {
            outState.putParcelableArrayList(SELECTED_DOCUMENTS_KEY,
                    new ArrayList<>(mSelectedDocuments));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAppBar.setExpanded(false, false);
        if (savedInstanceState.containsKey(SELECTED_DOCUMENTS_KEY)) {
            ArrayList<DocumentMetadata> docs = savedInstanceState.getParcelableArrayList(SELECTED_DOCUMENTS_KEY);
            mSelectedDocuments.addAll(docs);
            mAdapter.updateSelectedDocuments();
            updateSelectionBar();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_music) {
            Toast.makeText(this, "TODO MUSIC", Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_sort) {
            ChangeSortDialog.showDialog(this, new ChangeSortDialog.Callback() {
                @Override
                public void onSortChanged() {
                    updateAdapterComparator();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        // update add doc adapter placeholder
        if (percentage < PERCENTAGE_ADD_DOC_THRESHOLD) {
            if (mAdapter.getShowAddDoc()) {
                mAdapter.setShowAddDoc(false);
            }
        } else {
            if (!mAdapter.getShowAddDoc()) {
                mAdapter.setShowAddDoc(true);
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
                TasksUtils.createDocument(MainActivity.this, name, new Callbacks.FinishCallback<Long>() {
                    @Override
                    public void onFinish(Long id) {
                        // TODO open document
                    }
                });
            }
        });
    }

    @Override
    public void onDocumentClick(DocumentMetadata doc) {
        Toast.makeText(this, "TODO Doc click:"+doc.id, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDocumentLongClick(DocumentMetadata doc) {
        if (mSelectedDocuments.contains(doc)) {
            mSelectedDocuments.remove(doc);
        } else {
            mSelectedDocuments.add(doc);
        }
        mAdapter.updateSelectedDocuments();
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

    private class SelectionBarLongClickHandler implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Toast toast = Toast.makeText(MainActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
    }

    private void showNoSelectionMode() {
        mSelectionBar.setVisibility(View.INVISIBLE);
    }

    private void showSingleSelectionMode() {
        mEditSelectionBar.setVisibility(View.VISIBLE);
        mSelectionBar.setVisibility(View.VISIBLE);
    }

    private void showMultipleSelectionMode() {
        mEditSelectionBar.setVisibility(View.GONE);
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
        mAdapter.updateSelectedDocuments();
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

    @OnClick(R.id.main_selection_bar_cloud)
    public void onSelectionCloudClick() {
        // TODO
    }

    @OnClick(R.id.main_selection_bar_share)
    public void onSelectionShareClick() {
        // TODO
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
                        mAdapter.updateSelectedDocuments();
                    }
                });
    }

    @OnClick(R.id.main_selection_bar_edit)
    public void onSelectionSettingsClick() {
        final DocumentMetadata doc = mSelectedDocuments.iterator().next();
        EditDocDialog dialog = EditDocDialog.newInstance(doc);
        dialog.setCallback(new EditDocDialog.Callback() {
            @Override
            public void onColorChanged(@ColorInt int color) {
                TasksUtils.setCoverColor(MainActivity.this, doc, color, new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        mSelectedDocuments.clear();
                        showNoSelectionMode();
                        mAdapter.updateSelectedDocuments();
                    }
                });
            }
            @Override
            public void onImageChanged(String imagePath) {
                TasksUtils.setCoverImage(MainActivity.this, doc, imagePath, new Callbacks.FinishCallback<Void>() {
                    @Override
                    public void onFinish(Void result) {
                        mSelectedDocuments.clear();
                        showNoSelectionMode();
                        mAdapter.updateSelectedDocuments();
                    }
                });
            }
        });
        dialog.show(getSupportFragmentManager(), "EditDocDialogFragment");
    }

    @Override
    public void onAddDocumentClick() {
        onCreateDocumentClick();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }

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

}
