package io.github.nfdz.foco.ui.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Document;
import timber.log.Timber;

/**
 * This class is a DialogFragment implementation that manages the selection of a new background
 * color or image for the document cover.
 */
public class EditDocCoverDialog extends DialogFragment {

    public static final int IMAGE_MAX_WIDTH_PX = 500;
    public static final int IMAGE_MAX_HEIGHT_PX = 700;

    /**
     * Callback to be implemented to receive the new cover background color or image
     * if it has changed.
     */
    public interface Callback {
        void onColorChanged(@ColorInt int color);
        void onImageChanged(String imagePath);
    }

    /** Argument key of document metadata object stored in fragment arguments */
    public static final String DOC_ARG_KEY = "document";

    @BindView(R.id.dialog_edit_doc_tabs) TabLayout mTabLayout;
    @BindView(R.id.dialog_edit_doc_pager) ViewPager mViewPager;
    @BindView(R.id.dialog_edit_doc_cancel) View mCancelButton;
    @BindView(R.id.dialog_edit_doc_ok) View mOkButton;

    private PagerAdapter mAdapter;
    private DocumentMetadata mDocument;
    private Callback mCallback;

    /**
     * Creates a new instance of the fragment with the given document metadata in its arguments.
     * @param doc
     * @return EditDocCoverDialog fragment
     */
    public static EditDocCoverDialog newInstance(DocumentMetadata doc) {
        EditDocCoverDialog dialog = new EditDocCoverDialog();
        Bundle arg = new Bundle();
        arg.putParcelable(DOC_ARG_KEY, doc);
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // get document metadata from arguments
        mDocument = getArguments().getParcelable(DOC_ARG_KEY);

        return inflater.inflate(R.layout.dialog_edit_cover, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        // select tab depending of the current selected cover type
        if (savedInstanceState == null) {
            mViewPager.setCurrentItem(TextUtils.isEmpty(mDocument.coverImage) ? 0 : 1);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title and set background color
        // (thus the integration of old and new sdk versions is better)
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        return dialog;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            getDialog().dismiss();
        }
    }

    @OnClick(R.id.dialog_edit_doc_cancel)
    public void onCancelClick() {
        dismiss();
    }

    /**
     * This method processes current cover selection and performs needed operation (copy image
     * from external storage to internal, compress it, etc) and finally notify through callback.
     */
    @OnClick(R.id.dialog_edit_doc_ok)
    public void onOkClick() {
        // if there is no callback, avoid performs useless operations
        if (mCallback == null) {
            dismiss();
            return;
        }
        int position = mViewPager.getCurrentItem();
        switch (position) {
            case 0:
                // color tab is selected
                int currentColor = mDocument.coverColor;
                int newColor = mAdapter.mColorFragment.mColor;
                // notify only if color has changed
                if (currentColor != newColor) {
                    mCallback.onColorChanged(mAdapter.mColorFragment.mColor);
                }
                dismiss();
                return;
            case 1:
                // image tab is selected
                final String currentImage = mDocument.coverImage;
                final String newImage = mAdapter.mImageFragment.mImagePath;
                // notify only if image has changed
                if (currentImage.equals(newImage)) {
                    dismiss();
                    return;
                }
                // disable dialog fragment buttons meanwhile it is processing image operations
                // in background
                mOkButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        // copy and compress selected image to internal storage
                        ContextWrapper cw = new ContextWrapper(getActivity());
                        File directory = cw.getDir("media", Context.MODE_PRIVATE);
                        String randomName = UUID.randomUUID().toString();
                        try {
                            File compressedImage = new Compressor(getActivity())
                                    .setMaxWidth(IMAGE_MAX_WIDTH_PX)
                                    .setMaxHeight(IMAGE_MAX_HEIGHT_PX)
                                    .setQuality(25)
                                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                    .setDestinationDirectoryPath(directory.getAbsolutePath())
                                    .compressToFile(new File(newImage), randomName);
                            return compressedImage.getAbsolutePath();
                        } catch (IOException e) {
                            Timber.d(e);
                            return null;
                        }
                    }
                    @Override
                    protected void onPostExecute(String imagePath) {
                        // if image was processed correctly notify it
                        if (!TextUtils.isEmpty(imagePath)) {
                            mCallback.onImageChanged(imagePath);
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(),
                                    R.string.dialog_edit_doc_cover_image_process_msg,
                                    Toast.LENGTH_SHORT).show();
                            mOkButton.setEnabled(true);
                            mCancelButton.setEnabled(true);
                        }
                    }
                }.execute();
                return;
            default:
                Timber.e("Unexpected pager position: " + position);
        }

    }

    /**
     * Sets dialog fragment callback.
     * @param callback
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * Pager Adapter implementation with color and image fragment.
     */
    public class PagerAdapter extends FragmentPagerAdapter {

        final ColorFragment mColorFragment = ColorFragment.newInstance(mDocument.coverColor);
        final ImageFragment mImageFragment = ImageFragment.newInstance(mDocument.coverImage);

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mColorFragment;
                case 1:
                    return mImageFragment;
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
                    return getString(R.string.dialog_edit_doc_cover_color);
                case 1:
                    return getString(R.string.dialog_edit_doc_cover_image);
                default:
                    Timber.e("Unexpected get title page position: " + position);
                    return null;
            }
        }
    }

    /**
     * Color inner fragment. It contains needed views to visualize and modify color.
     */
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

        int mColor;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int color = getArguments().getInt(COLOR_ARG_KEY);
            mColor = color == Document.NULL_COVER_COLOR ? Document.DEFAULT_COVER_COLOR : color;

            View view = inflater.inflate(R.layout.fragment_edit_cover_color, container, false);
            ButterKnife.bind(this, view);
            updateColorView();

            return view;
        }

        /**
         * Update color text and sample views.
         */
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
            // opens a color picker dialog to ease to select a new color
            final ColorPicker cp = new ColorPicker(getActivity(),
                    Color.red(mColor),
                    Color.green(mColor),
                    Color.blue(mColor));
            cp.show();
            ((Button)cp.findViewById(R.id.okColorButton)).setText(R.string.dialog_edit_doc_cover_color_select_ok);
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

    /**
     * Image inner fragment. It contains needed views to visualize and modify image.
     */
    public static class ImageFragment extends Fragment {

        public static final String IMAGE_ARG_KEY = "image";
        public static final int PERMISSION_REQUEST_CODE = 2372;

        public static ImageFragment newInstance(String imagePath) {
            ImageFragment fragment = new ImageFragment();
            Bundle arg = new Bundle();
            arg.putString(IMAGE_ARG_KEY, imagePath);
            fragment.setArguments(arg);
            return fragment;
        }

        @BindView(R.id.edit_cover_image_path) TextView mImagePathText;
        @BindView(R.id.edit_cover_image) ImageView mImageView;

        String mImagePath;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mImagePath = getArguments().getString(IMAGE_ARG_KEY);

            View view = inflater.inflate(R.layout.fragment_edit_cover_image, container, false);
            ButterKnife.bind(this, view);
            updateImage();

            return view;
        }

        /**
         * Update image path text and image views.
         */
        private void updateImage() {
            mImagePathText.setText(TextUtils.isEmpty(mImagePath) ? "-" : mImagePath);
            if (TextUtils.isEmpty(mImagePath)) {
                // if there is no selected image, load image placeholder
                Picasso.with(getActivity())
                        .load(R.drawable.image_placeholder)
                        .into(mImageView);
            } else {
                // It is important to be very careful loading this image because it can be
                // high resolution, very heavy and cause application memory problems
                // (there is no danger of out of memory thanks to picasso)
                Picasso.with(getActivity())
                        .load(new File(mImagePath))
                        .placeholder(R.drawable.image_placeholder)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(IMAGE_MAX_WIDTH_PX, IMAGE_MAX_HEIGHT_PX)
                        .centerInside()
                        .onlyScaleDown()
                        .into(mImageView);
            }
        }

        @OnClick(R.id.edit_cover_image)
        public void onImageClick() {
            onSelectImageClick();
        }

        @OnClick(R.id.edit_cover_image_button)
        public void onSelectImageClick() {
            // if version is equals or greater than android M, ask permissions to access external storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openFilePickerDialog();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePickerDialog();
                } else {
                    // if user rejects permission, notify it
                    Toast.makeText(getActivity(),
                            R.string.dialog_edit_doc_cover_image_permissions_msg,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * Open a file picker dialog to ease select a new image from external storage.
         */
        public void openFilePickerDialog() {
            DialogProperties properties = new DialogProperties();

            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            // initial directory should be pictures directory
            properties.offset = new File("/mnt/sdcard" + File.separator + Environment.DIRECTORY_PICTURES);
            // show accepted image format only
            properties.extensions = new String[]{"jpg","jpeg","png","gif","bmp","webp"};

            FilePickerDialog dialog = new FilePickerDialog(getActivity(), properties);
            dialog.setTitle(R.string.dialog_edit_doc_cover_image_select_title);
            dialog.setPositiveBtnName(getString(R.string.dialog_edit_doc_cover_image_select_ok));
            dialog.setNegativeBtnName(getString(android.R.string.cancel));

            dialog.setDialogSelectionListener(new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    if (files.length > 0) {
                        mImagePath = files[0];
                        updateImage();
                    }
                }
            });

            dialog.show();

            // There is a problem with library dialog theme, it does not support a light
            // primary color because header text color is always white.
            // With this hack (we had to study library layout) it ensures that header background
            // is ok.
            dialog.findViewById(R.id.header).setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorAccent));
        }
    }

}