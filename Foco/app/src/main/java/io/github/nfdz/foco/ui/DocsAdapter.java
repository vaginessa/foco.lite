package io.github.nfdz.foco.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.model.Document;
import io.github.nfdz.foco.utils.DocItemUtils;
import io.github.nfdz.foco.utils.FontChangeCrawler;

/**
 * Recycler view adapter implementation. It uses an inner custom view holder implementation.
 */
public class DocsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DOCUMENT_TYPE = 0;
    private static final int ADD_DOCUMENT_TYPE = 1;

    private static final String EDITION_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    /**
     * Clicks handler interface.
     */
    public interface DocsClickHandler {
        void onDocumentClick(DocumentMetadata doc);
        void onDocumentLongClick(DocumentMetadata doc);
        void onAddDocumentClick();
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
    private boolean mShowAddDoc;
    private Set<DocumentMetadata> mSelectedDocuments;

    public DocsAdapter(@NonNull Context context,
                       @Nullable Set<DocumentMetadata> selectedDocuments,
                       @Nullable DocsClickHandler clickHandler) {
        mContext = context;
        mHandler = clickHandler;
        mSelectedDocuments = selectedDocuments == null ? new HashSet<DocumentMetadata>() :
                selectedDocuments;
        mShowAddDoc = false;
        mRegularFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_regular));
        mBoldFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_bold));
    }

    public void setDocumentList(List<DocumentMetadata> docs) {
        mDocs = docs;
        sort();
        filter();
        notifyDataSetChanged();
    }

    public void setShowAddDoc(boolean showAddDoc) {
        mShowAddDoc = showAddDoc;
        notifyDataSetChanged();
    }

    public void setComparator(@Nullable Comparator<Document> comparator) {
        mComparator = comparator;
        sort();
        notifyDataSetChanged();
    }

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

    public boolean getShowAddDoc() {
        return mShowAddDoc;
    }

    public void updateSelectedDocuments() {
        notifyDataSetChanged();
    }

    private void sort() {
        if (mComparator != null && mDocs != null && !mDocs.isEmpty()) {
            Collections.sort(mDocs, mComparator);
        }
    }

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
        if (viewType == ADD_DOCUMENT_TYPE) {
            int layoutId = R.layout.add_doc_list_item;
            LayoutInflater inflater = LayoutInflater.from(mContext);
            boolean shouldAttachToParent = false;
            View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
            return new AddDocViewHolder(view);
        } else {
            int layoutId = R.layout.doc_list_item;
            LayoutInflater inflater = LayoutInflater.from(mContext);
            boolean shouldAttachToParent = false;
            View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
            return new DocViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == DOCUMENT_TYPE) {
            DocViewHolder docHolder = (DocViewHolder) holder;
            DocumentMetadata doc = mFilteredDocs.get(position);

            DocItemUtils.resolveTitleSize(mContext, doc.getName(), docHolder.title);

            if (TextUtils.isEmpty(mFilterText)) {
                docHolder.title.setText(doc.getName());
            } else {
                Spannable titleSpan = new SpannableString(doc.getName());
                int color = ContextCompat.getColor(mContext, R.color.highlightTextColor);
                int startHighlight = doc.getName().toLowerCase().indexOf(mFilterText);
                int endHighlight = startHighlight + mFilterText.length();
                titleSpan.setSpan(new ForegroundColorSpan(color),
                        startHighlight,
                        endHighlight,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                docHolder.title.setText(titleSpan);
            }

            if (doc.getWords() != Document.NULL_WORDS) {
                int words = doc.getWords();
                StringBuilder bld = new StringBuilder();
                bld.append(words);
                bld.append(words == 1 ? " word" : " words");
                docHolder.words.setText(bld.toString());
                docHolder.words.setVisibility(View.VISIBLE);
            } else {
                docHolder.words.setVisibility(View.GONE);
            }

            if (doc.getWorkingTimeMillis() != Document.NULL_WORKING_TIME) {
                docHolder.workTime.setText(getWorkingTimeText(doc.getWorkingTimeMillis()));
                docHolder.workTime.setVisibility(View.VISIBLE);
            } else {
                docHolder.workTime.setVisibility(View.GONE);
            }

            if (doc.getLastEditionTimeMillis() != Document.NULL_LAST_EDITION_TIME) {
                Date editionDate = new Date(doc.getLastEditionTimeMillis());
                String editionDateStr = mTimeFormat.format(editionDate);
                docHolder.editTime.setText(editionDateStr);
                docHolder.editTime.setVisibility(View.VISIBLE);
            } else {
                docHolder.editTime.setVisibility(View.GONE);
            }

            if (doc.isFavorite()) {
                docHolder.fav.setVisibility(View.VISIBLE);
            } else {
                docHolder.fav.setVisibility(View.INVISIBLE);
            }

            // if there is an image load image, if not load color
            if (!TextUtils.isEmpty(doc.getCoverImage())) {
                Picasso.with(mContext)
                        .load(new File(doc.getCoverImage()))
                        .placeholder(R.drawable.image_placeholder)
                        .into(docHolder.bg);
            } else if (doc.getCoverColor() != Document.NULL_COVER_COLOR) {
                Picasso.with(mContext).cancelRequest(docHolder.bg);
                docHolder.bg.setImageDrawable(null);
                docHolder.bg.setBackgroundColor(doc.getCoverColor());
            } else {
                Picasso.with(mContext).cancelRequest(docHolder.bg);
                docHolder.bg.setImageDrawable(null);
                docHolder.bg.setBackgroundColor(Document.DEFAULT_COVER_COLOR);;
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
    }

    public static String getWorkingTimeText(long workingTime) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(workingTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(workingTime);
        long hours = TimeUnit.MILLISECONDS.toHours(workingTime);
        long days = TimeUnit.MILLISECONDS.toDays(workingTime);

        if (days > 0) {
            return days + (days == 1 ? " day" : " days");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour" : " hours");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        } else {
            return seconds + (seconds == 1 ? " second" : " seconds");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mShowAddDoc && isTheLastOne(position) ? ADD_DOCUMENT_TYPE : DOCUMENT_TYPE;
    }

    private boolean isTheLastOne(int position) {
        int listSize = mFilteredDocs != null ? mFilteredDocs.size() : 0;
        return position == listSize;
    }

    @Override
    public int getItemCount() {
        int listSize = mFilteredDocs != null ? mFilteredDocs.size() : 0;
        return listSize + (mShowAddDoc ? 1 : 0);
    }

    public class DocViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.doc_item_title) TextView title;
        @BindView(R.id.doc_item_work_time) TextView workTime;
        @BindView(R.id.doc_item_words) TextView words;
        @BindView(R.id.doc_item_edit_time) TextView editTime;
        @BindView(R.id.doc_item_fav) ImageView fav;
        @BindView(R.id.doc_item_bg) ImageView bg;

        public DocViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            // replace fonts
            mBoldFontChanger.replaceFonts(title);
            mRegularFontChanger.replaceFonts(workTime);
            mRegularFontChanger.replaceFonts(words);
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

    public class AddDocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public AddDocViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mHandler != null) mHandler.onAddDocumentClick();
        }
    }
}
