package io.github.nfdz.foco.ui;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.AppDatabase;
import io.github.nfdz.foco.data.entity.DocumentEntity;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.viewmodel.DocListViewModel;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        DocsAdapter.DocsClickHandler, LifecycleRegistryOwner {

    private static final String SELECTED_DOCUMENTS_KEY = "selected-documents";

    private static final float PERCENTAGE_ADD_DOC_THRESHOLD = 0.7f;
    private static final float PERCENTAGE_LOGO_THRESHOLD = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    private final Set<Integer> mSelectedDocumentsIds = new HashSet<>();

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
    @BindView(R.id.main_selection_bar_settings) ImageButton mSettingsSelectionBar;

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
        mSettingsSelectionBar.setOnLongClickListener(handler);

        // set up recycler view
        int spanCount = getResources().getInteger(R.integer.grid_doc_columns);
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        mLayoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DocsAdapter(this, mSelectedDocumentsIds, this);
        mRecyclerView.setAdapter(mAdapter);

        mAppBar.addOnOffsetChangedListener(this);
        startAlphaAnimation(mToolbarLogo, 0, View.INVISIBLE);

        showLoading();
        DocListViewModel viewModel = ViewModelProviders.of(this).get(DocListViewModel.class);
        subscribeUi(viewModel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(SELECTED_DOCUMENTS_KEY, new ArrayList<Integer>(mSelectedDocumentsIds));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAppBar.setExpanded(false, false);
        mSelectedDocumentsIds.addAll(savedInstanceState.getIntegerArrayList(SELECTED_DOCUMENTS_KEY));
        mAdapter.updateSelectedDocuments();
        updateSelectionBar();
    }

    @Override
    public void onBackPressed() {
        if (mSelectedDocumentsIds.size() != 0) {
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
        if (id == R.id.action_settings) {
            Toast.makeText(this, "TODO SETTINGS", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        //Snackbar.make(mFab, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        // insert dummy doc
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
            DocumentEntity doc = new DocumentEntity();
            doc.setLastEditionTimeMillis(System.currentTimeMillis());
            doc.setName("Testing Title");
            doc.setText("ASDFASFASDF ASDF ASDF ASDF AS FASF AS DF");
            doc.setWorkingTimeMillis(new Random().nextInt());
            AppDatabase.getInstance(MainActivity.this).documentDao().insert(doc);
            return null;
            }
        }.execute();

    }

    @Override
    public void onDocumentClick(DocumentMetadata doc) {
        Toast.makeText(this, "TODO Doc click:"+doc.id, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDocumentLongClick(DocumentMetadata doc) {
        if (mSelectedDocumentsIds.contains(doc.id)) {
            mSelectedDocumentsIds.remove(doc.id);
        } else {
            mSelectedDocumentsIds.add(doc.id);
        }
        mAdapter.updateSelectedDocuments();
        updateSelectionBar();
    }

    private void updateSelectionBar() {
        switch (mSelectedDocumentsIds.size()) {
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
        mSettingsSelectionBar.setVisibility(View.VISIBLE);
        mSelectionBar.setVisibility(View.VISIBLE);
    }

    private void showMultipleSelectionMode() {
        mSettingsSelectionBar.setVisibility(View.GONE);
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
        mSelectedDocumentsIds.clear();
        mAdapter.updateSelectedDocuments();
        showNoSelectionMode();
    }

    @OnClick(R.id.main_selection_bar_delete)
    public void onSelectionDeleteClick() {
        // TODO
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
        // TODO
    }

    @OnClick(R.id.main_selection_bar_settings)
    public void onSelectionSettingsClick() {
        // TODO
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
