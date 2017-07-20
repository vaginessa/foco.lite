package io.github.nfdz.foco.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private Comparator<Document> mComparator;
    private List<DocumentMetadata> mDocs;
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
        notifyDataSetChanged();
    }

    public void setShowAddDoc(boolean showAddDoc) {
        mShowAddDoc = showAddDoc;
        notifyDataSetChanged();
    }

    public void setComparator(Comparator<Document> comparator) {
        mComparator = comparator;
        sort();
        notifyDataSetChanged();
    }

    public boolean getShowAddDoc() {
        return mShowAddDoc;
    }

    public void updateSelectedDocuments() {
        notifyDataSetChanged();
    }

    private void sort() {
        if (mComparator != null && mDocs != null) {
            Collections.sort(mDocs, mComparator);
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
            DocumentMetadata doc = mDocs.get(position);
            DocItemUtils.resolveTitleSize(mContext, doc.name, docHolder.title);
            docHolder.title.setText(doc.name);

            if (doc.words != Document.NULL_WORDS) {
                docHolder.words.setText("1500 words");
                docHolder.words.setVisibility(View.VISIBLE);
            } else {
                docHolder.words.setVisibility(View.GONE);
            }

            if (doc.workingTime != Document.NULL_WORKING_TIME) {
                docHolder.workTime.setText("40 minutes");
                docHolder.workTime.setVisibility(View.VISIBLE);
            } else {
                docHolder.workTime.setVisibility(View.GONE);
            }

            if (doc.lastEditionTime != Document.NULL_LAST_EDITION_TIME) {
                docHolder.editTime.setText("1999/12/31 00:00");
                docHolder.editTime.setVisibility(View.VISIBLE);
            } else {
                docHolder.editTime.setVisibility(View.GONE);
            }

            if (doc.isFavorite) {
                docHolder.fav.setVisibility(View.VISIBLE);
            } else {
                docHolder.fav.setVisibility(View.INVISIBLE);
            }

            // if there is an image load image, if not load color
            if (!TextUtils.isEmpty(doc.coverImage)) {
                Picasso.with(mContext)
                        .load(new File(doc.coverImage))
                        .placeholder(R.drawable.image_placeholder)
                        .into(docHolder.bg);
            } else if (doc.coverColor != Document.NULL_COVER_COLOR) {
                Picasso.with(mContext).cancelRequest(docHolder.bg);
                docHolder.bg.setImageDrawable(null);
                docHolder.bg.setBackgroundColor(doc.coverColor);
            } else {
                Picasso.with(mContext).cancelRequest(docHolder.bg);
                docHolder.bg.setImageDrawable(null);
                docHolder.bg.setBackgroundColor(Document.DEFAULT_COVER_COLOR);;
            }

            // check if document is selected
            boolean selected = false;
            for (DocumentMetadata selectedDoc : mSelectedDocuments) {
                if (selectedDoc.id == doc.id) {
                    selected = true;
                    break;
                }
            }
            docHolder.itemView.setSelected(selected);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mShowAddDoc && isTheLastOne(position) ? ADD_DOCUMENT_TYPE : DOCUMENT_TYPE;
    }

    private boolean isTheLastOne(int position) {
        int listSize = mDocs != null ? mDocs.size() : 0;
        return position == listSize;
    }

    @Override
    public int getItemCount() {
        int listSize = mDocs != null ? mDocs.size() : 0;
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
                        mHandler.onDocumentClick(mDocs.get(pos));
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mHandler != null) {
                        int pos = getAdapterPosition();
                        mHandler.onDocumentLongClick(mDocs.get(pos));
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
