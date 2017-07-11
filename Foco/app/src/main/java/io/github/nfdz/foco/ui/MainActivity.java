package io.github.nfdz.foco.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;

public class MainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_LOGO_THRESHOLD = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mToolbarLogoVisible = false;
    private boolean mLayoutLogoVisible = true;

    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.main_fab_add) FloatingActionButton mFab;
    @BindView(R.id.main_toolbar_image_logo) ImageView mToolbarLogo;
    @BindView(R.id.main_layout_image_logo) ImageView mLayoutLogo;
    @BindView(R.id.main_app_bar) AppBarLayout mAppBar;

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

    @OnClick(R.id.main_fab_add)
    void onCreateDocumentClick() {
        Snackbar.make(mFab, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
}
