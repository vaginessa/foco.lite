package io.github.nfdz.focolite.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This class has static methods that ease work with custom fonts and text views.
 */
public class FontChangeCrawler {

    public static final String ASSETS_FONT_FOLDER = "font";

    private Typeface mTypeface;

    public FontChangeCrawler(AssetManager assets, String assetsFontFileName) {
        mTypeface = Typeface.createFromAsset(assets, ASSETS_FONT_FOLDER + "/" + assetsFontFileName);
    }

    public void replaceFonts(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewTree = (ViewGroup) view;
            View child;
            for (int i = 0; i < viewTree.getChildCount(); ++i) {
                child = viewTree.getChildAt(i);
                if (child instanceof ViewGroup) {
                    replaceFonts((ViewGroup)child);
                }
                else if (child instanceof TextView) {
                    ((TextView) child).setTypeface(mTypeface);
                }
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTypeface(mTypeface);
        }

    }
}
