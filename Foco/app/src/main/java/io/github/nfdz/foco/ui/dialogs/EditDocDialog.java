package io.github.nfdz.foco.ui.dialogs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Document;
import timber.log.Timber;

public class EditDocDialog extends DialogFragment {

    public interface Callback {
        void onColorChanged(@ColorInt int color);
        void onImageChanged(String imagePath);
    }

    public static final String DOC_ARG_KEY = "document";

    @BindView(R.id.dialog_edit_doc_tabs) TabLayout mTabLayout;
    @BindView(R.id.dialog_edit_doc_pager) ViewPager mViewPager;

    private PagerAdapter mAdapter;
    private DocumentMetadata mDocument;
    private Callback mCallback;

    public static EditDocDialog newInstance(DocumentMetadata doc) {
        EditDocDialog dialog = new EditDocDialog();
        Bundle arg = new Bundle();
        arg.putParcelable(DOC_ARG_KEY, doc);
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDocument = getArguments().getParcelable(DOC_ARG_KEY);
        return inflater.inflate(R.layout.dialog_edit_doc, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getDialog().setTitle("Edit document cover");

        mAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


        mTabLayout = (TabLayout) view.findViewById(R.id.dialog_edit_doc_tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.dialog_edit_doc_pager);
    }

    @OnClick(R.id.dialog_edit_doc_cancel)
    public void onCancelClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_edit_doc_ok)
    public void onOkClick() {
        int position = mViewPager.getCurrentItem();
        switch (position) {
            case 0:
                if (mCallback != null) mCallback.onColorChanged(mAdapter.mColorFragment.mColor);
                dismiss();
                return;
            case 1:
                dismiss();
                return;
            default:
                Timber.e("Unexpected pager position: " + position);
        }

    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        final ColorFragment mColorFragment = ColorFragment.newInstance(mDocument.coverColor);

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mColorFragment;
                case 1:
                    return ColorFragment.newInstance(mDocument.coverColor);
                default:
                    Timber.e("Unexpected get item page position: " + position);
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Color";
                case 1:
                    return "Image";
                default:
                    Timber.e("Unexpected get title page position: " + position);
                    return null;
            }
        }
    }

    public static class ColorFragment extends Fragment {

        public static final String COLOR_ARG_KEY = "color";

        public static ColorFragment newInstance(@ColorInt int color) {
            ColorFragment fragment = new ColorFragment();
            Bundle arg = new Bundle();
            arg.putInt(COLOR_ARG_KEY, color);
            fragment.setArguments(arg);
            return fragment;
        }

        @BindView(R.id.edit_cover_color_text) TextView mColorText;
        @BindView(R.id.edit_cover_color_sample) View mSampleView;

        private int mColor;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int color = getArguments().getInt(COLOR_ARG_KEY);
            mColor = color == Document.NULL_COVER_COLOR ? Document.DEFAULT_COVER_COLOR : color;

            View view = inflater.inflate(R.layout.fragment_edit_cover_color, container, false);
            ButterKnife.bind(this, view);
            updateColorView();

            return view;
        }

        private void updateColorView() {
            String colorTextWithoutAlpha = String.format("#%06X", (0xFFFFFF & mColor));
            mColorText.setText(colorTextWithoutAlpha);
            mSampleView.setBackgroundColor(mColor);
        }

        @OnClick(R.id.edit_cover_color_sample)
        public void onSampleClick() {
            onSelectColorClick();
        }

        @OnClick(R.id.edit_cover_color_button)
        public void onSelectColorClick() {
            final ColorPicker cp = new ColorPicker(getActivity(),
                    Color.red(mColor),
                    Color.green(mColor),
                    Color.blue(mColor));
            cp.show();
            cp.setCallback(new ColorPickerCallback() {
                @Override
                public void onColorChosen(@ColorInt int color) {
                    mColor = color;
                    updateColorView();
                    cp.dismiss();
                }
            });
        }
    }

}
