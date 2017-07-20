package io.github.nfdz.foco.ui.dialogs;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;
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

public class EditDocDialog extends DialogFragment {

    public interface Callback {
        void onColorChanged(@ColorInt int color);

        void onImageChanged(String imagePath);
    }

    public static final String DOC_ARG_KEY = "document";

    @BindView(R.id.dialog_edit_doc_tabs) TabLayout mTabLayout;
    @BindView(R.id.dialog_edit_doc_pager) ViewPager mViewPager;
    @BindView(R.id.dialog_edit_doc_cancel) View mCancelButton;
    @BindView(R.id.dialog_edit_doc_ok) View mOkButton;

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
        if (mCallback == null) {
            dismiss();
            return;
        }
        int position = mViewPager.getCurrentItem();
        switch (position) {
            case 0:
                mCallback.onColorChanged(mAdapter.mColorFragment.mColor);
                dismiss();
                return;
            case 1:
                mOkButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String currentImage = mDocument.coverImage;
                        String newImage = mAdapter.mImageFragment.mImagePath;
                        if (!TextUtils.isEmpty(currentImage)) {
                            File file = new File(currentImage);
                            file.delete();
                        }
                        ContextWrapper cw = new ContextWrapper(getActivity());
                        File directory = cw.getDir("media", Context.MODE_PRIVATE);
                        String randomName = UUID.randomUUID().toString();
                        try {
                            File compressedImage = new Compressor(getActivity())
                                    .setMaxWidth(700)
                                    .setMaxHeight(800)
                                    .setQuality(75)
                                    .setCompressFormat(Bitmap.CompressFormat.PNG)
                                    .setDestinationDirectoryPath(directory.getAbsolutePath())
                                    .compressToFile(new File(newImage), randomName);
                            return compressedImage.getAbsolutePath();
                        } catch (IOException e) {
                            return null;
                        }
                    }
                    @Override
                    protected void onPostExecute(String imagePath) {
                        if (!TextUtils.isEmpty(imagePath)) {
                            mCallback.onImageChanged(imagePath);
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(),
                                    "Cannot create document image cover from selected image.",
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

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

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

        private String mImagePath;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mImagePath = getArguments().getString(IMAGE_ARG_KEY);

            View view = inflater.inflate(R.layout.fragment_edit_cover_image, container, false);
            ButterKnife.bind(this, view);
            updateImage();

            return view;
        }

        private void updateImage() {
            mImagePathText.setText(TextUtils.isEmpty(mImagePath) ? "-" : mImagePath);
            if (TextUtils.isEmpty(mImagePath)) {
                Picasso.with(getActivity())
                        .load(R.drawable.image_placeholder)
                        .into(mImageView);
            } else {
                Picasso.with(getActivity())
                        .load(new File(mImagePath))
                        .placeholder(R.drawable.image_placeholder)
                        .into(mImageView);
            }
        }

        @OnClick(R.id.edit_cover_image)
        public void onImageClick() {
            onSelectImageClick();
        }

        @OnClick(R.id.edit_cover_image_button)
        public void onSelectImageClick() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openFilePickerDialog();
            }
        }

        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            // TODO check if this works
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePickerDialog();
                } else {
                    Toast.makeText(getActivity(),
                            "Permission is required for getting list of images",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void openFilePickerDialog() {
            DialogProperties properties = new DialogProperties();

            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.offset = new File("/mnt/sdcard" + File.separator + Environment.DIRECTORY_PICTURES);
            properties.extensions = new String[]{"jpg","jpeg","png","gif","bmp","webp"};

            FilePickerDialog dialog = new FilePickerDialog(getActivity(), properties);
            dialog.setTitle("Select a File");

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

            dialog.findViewById(R.id.header).setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.colorAccent));
        }
    }

}