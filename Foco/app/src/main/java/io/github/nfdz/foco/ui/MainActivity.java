package io.github.nfdz.foco.ui;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.viewmodel.DocListViewModel;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        DocsAdapter.DocsClickHandler, LifecycleRegistryOwner {

    private static final float PERCENTAGE_LOGO_THRESHOLD = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    private boolean mToolbarLogoVisible = false;
    private boolean mLayoutLogoVisible = true;

    private DocsAdapter mAdapter;

    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.main_fab_add) FloatingActionButton mFab;
    @BindView(R.id.main_toolbar_image_logo) ImageView mToolbarLogo;
    @BindView(R.id.main_layout_image_logo) ImageView mLayoutLogo;
    @BindView(R.id.main_app_bar) AppBarLayout mAppBar;
    @BindView(R.id.main_rv_docs) RecyclerView mRecyclerView;
    @BindView(R.id.main_loading) ProgressBar mLoading;
    @BindView(R.id.main_collapsing_layout) CollapsingToolbarLayout mCollapsingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        mAppBar.addOnOffsetChangedListener(this);

        startAlphaAnimation(mToolbarLogo, 0, View.INVISIBLE);

        int spanCount = getResources().getInteger(R.integer.grid_doc_columns);
        int orientation = OrientationHelper.VERTICAL;
        boolean reverseLayout = false;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount, orientation, reverseLayout);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DocsAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        showLoading();
        DocListViewModel viewModel = ViewModelProviders.of(this).get(DocListViewModel.class);
        subscribeUi(viewModel);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAppBar.setExpanded(false, false);
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

    public static void startAlphaAnimation(View v, long duration, int visibility) {
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
            if(!mToolbarLogoVisible) {
                startAlphaAnimation(mToolbarLogo, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mToolbarLogoVisible = true;
            }
            if(mLayoutLogoVisible) {
                startAlphaAnimation(mLayoutLogo, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mLayoutLogoVisible = false;
            }
        }

    }

    private void updateCollapsingBehaviour(){
        /*if (mLayoutManager.findLastCompletelyVisibleItemPosition() == items.size()-1) {
            turnOffToolbarScrolling();
        } else {
            turnOnToolbarScrolling();
        }*/
    }

    private void turnOffCollapsing() {
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) mCollapsingLayout.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(0);
        mCollapsingLayout.setLayoutParams(toolbarLayoutParams);

        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
        appBarLayoutParams.setBehavior(null);
        mAppBar.setLayoutParams(appBarLayoutParams);
    }

    private void turnOnCollapsing() {
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) mCollapsingLayout.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingLayout.setLayoutParams(toolbarLayoutParams);

        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
        appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
        mAppBar.setLayoutParams(appBarLayoutParams);
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
        Snackbar.make(mFab, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onDocumentClick() {

    }

    @Override
    public void onDocumentLongClick() {

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
