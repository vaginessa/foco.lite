package io.github.nfdz.foco.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Document;
import io.github.nfdz.foco.utils.FontChangeCrawler;

/**
 * Recycler view adapter implementation. It uses an inner custom view holder implementation.
 */
public class DocsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String EDITION_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    /**
     * Clicks handler interface.
     */
    public interface DocsClickHandler {
        void onDocumentClick(DocumentMetadata doc);
        void onDocumentLongClick(DocumentMetadata doc);
    }

    private final Context mContext;
    private final DocsClickHandler mHandler;
    private final FontChangeCrawler mRegularFontChanger;
    private final FontChangeCrawler mBoldFontChanger;
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat(EDITION_TIME_PATTERN);

    private Comparator<Document> mComparator;
    private String mFilterText;
    private List<DocumentMetadata> mDocs;
    private List<DocumentMetadata> mFilteredDocs;
    private Set<DocumentMetadata> mSelectedDocuments;

    /**
     * Default constructor.
     * @param context
     * @param selectedDocuments This set is used to add/remove selected documents.
     * @param clickHandler
     */
    public DocsAdapter(@NonNull Context context,
                       @Nullable Set<DocumentMetadata> selectedDocuments,
                       @Nullable DocsClickHandler clickHandler) {
        mContext = context;
        mHandler = clickHandler;
        mSelectedDocuments = selectedDocuments == null ? new HashSet<DocumentMetadata>() :
                selectedDocuments;
        mRegularFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_regular));
        mBoldFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_bold));
    }

    /**
     * Sets document list data. It performs notifyDataSetChanged.
     * @param docs
     */
    public void setDocumentList(List<DocumentMetadata> docs) {
        mDocs = docs;
        sort();
        filter();
        notifyDataSetChanged();
    }

    /**
     * This method updates document comparator. It sorts and filters data and
     * performs notifyDataSetChanged.
     * @param comparator
     */
    public void setComparator(@Nullable Comparator<Document> comparator) {
        mComparator = comparator;
        sort();
        filter();
        notifyDataSetChanged();
    }

    /**
     * This method updates document filter text. It filter data and performs notifyDataSetChanged.
     * @param filterText
     */
    public void setFilter(@Nullable String filterText) {
        mFilterText = filterText != null ? filterText.toLowerCase() : null;
        filter();
        notifyDataSetChanged();
    }

    public boolean hasFilter() {
        return !TextUtils.isEmpty(mFilterText);
    }

    public String getFilterText() {
        return mFilterText;
    }

    /**
     * This method refreshes views with the information of the selected document set.
     * This really only performs notifyDataSetChanged.
     */
    public void refreshSelectedDocuments() {
        notifyDataSetChanged();
    }

    /**
     * Sorts unfiltered data set.
     */
    private void sort() {
        if (mComparator != null && mDocs != null && !mDocs.isEmpty()) {
            Collections.sort(mDocs, mComparator);
        }
    }

    /**
     * Filters unfiltered data set.
     */
    private void filter() {
        if (!TextUtils.isEmpty(mFilterText) && mDocs != null && !mDocs.isEmpty()) {
            mFilteredDocs = new ArrayList<>();
            for (DocumentMetadata doc : mDocs) {
                if (doc.getName().toLowerCase().indexOf(mFilterText) > -1) {
                    mFilteredDocs.add(doc);
                }
            }
        } else {
            mFilteredDocs = mDocs;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.doc_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParent = false;
        View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DocViewHolder docHolder = (DocViewHolder) holder;
        DocumentMetadata doc = mFilteredDocs.get(position);

        // update title label
        if (TextUtils.isEmpty(mFilterText)) {
            docHolder.title.setText(doc.getName());
        } else {
            // highlight text
            Spannable titleSpan = new SpannableString(doc.getName());
            int fgColor = ContextCompat.getColor(mContext, R.color.highlightTextColor);
            int bgColor = ContextCompat.getColor(mContext, R.color.highlightBgColor);
            int startHighlight = doc.getName().toLowerCase().indexOf(mFilterText);
            int endHighlight = startHighlight + mFilterText.length();
            titleSpan.setSpan(new ForegroundColorSpan(fgColor),
                    startHighlight,
                    endHighlight,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleSpan.setSpan(new BackgroundColorSpan(bgColor),
                    startHighlight,
                    endHighlight,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            docHolder.title.setText(titleSpan);
        }

        // update last edition time label
        if (doc.getLastEditionTimeMillis() != Document.NULL_LAST_EDITION_TIME) {
            Date editionDate = new Date(doc.getLastEditionTimeMillis());
            String editionDateStr = mTimeFormat.format(editionDate);
            docHolder.editTime.setText(editionDateStr);
            docHolder.editTime.setVisibility(View.VISIBLE);
        } else {
            docHolder.editTime.setVisibility(View.GONE);
        }

        // show/hide favorite icon
        if (doc.isFavorite()) {
            docHolder.fav.setVisibility(View.VISIBLE);
        } else {
            docHolder.fav.setVisibility(View.INVISIBLE);
        }

        // check if document is selected
        boolean selected = false;
        for (DocumentMetadata selectedDoc : mSelectedDocuments) {
            if (selectedDoc.id == doc.getId()) {
                selected = true;
                break;
            }
        }
        docHolder.itemView.setSelected(selected);
    }

    @Override
    public int getItemCount() {
        return mFilteredDocs != null ? mFilteredDocs.size() : 0;
    }

    public class DocViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.doc_item_title) TextView title;
        @BindView(R.id.doc_item_edit_time) TextView editTime;
        @BindView(R.id.doc_item_fav) ImageView fav;

        public DocViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            // replace fonts
            mBoldFontChanger.replaceFonts(title);
            mRegularFontChanger.replaceFonts(editTime);

            // set click listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHandler != null) {
                        int pos = getAdapterPosition();
                        mHandler.onDocumentClick(mFilteredDocs.get(pos));
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mHandler != null) {
                        int pos = getAdapterPosition();
                        mHandler.onDocumentLongClick(mFilteredDocs.get(pos));
                    }
                    return true;
                }
            });
        }
    }
}
