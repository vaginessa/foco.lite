package io.github.nfdz.foco.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.foco.R;
import io.github.nfdz.foco.data.entity.DocumentMetadata;
import io.github.nfdz.foco.utils.DocItemUtils;
import io.github.nfdz.foco.utils.FontChangeCrawler;

public class DocsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DOCUMENT_TYPE = 0;
    private static final int ADD_DOCUMENT_TYPE = 1;

    public interface DocsClickHandler {
        void onDocumentClick(DocumentMetadata doc);
        void onDocumentLongClick(DocumentMetadata doc);
        void onAddDocumentClick();
    }

    private final Context mContext;
    private final DocsClickHandler mHandler;
    private final FontChangeCrawler mRegularFontChanger;
    private final FontChangeCrawler mBoldFontChanger;

    private List<DocumentMetadata> mDocs;
    private boolean mShowAddDoc;
    private Set<Integer> mSelectedDocumentsIds;


    public DocsAdapter(@NonNull Context context,
                       @Nullable Set<Integer> selectedDocumentsIds,
                       @Nullable DocsClickHandler clickHandler) {
        mContext = context;
        mHandler = clickHandler;
        mSelectedDocumentsIds = selectedDocumentsIds == null ? new HashSet<Integer>() :
                selectedDocumentsIds;
        mShowAddDoc = false;
        mRegularFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_regular));
        mBoldFontChanger = new FontChangeCrawler(mContext.getAssets(),
                mContext.getString(R.string.font_libre_baskerville_bold));
    }

    public void setDocumentList(List<DocumentMetadata> docs) {
        mDocs = docs;
        notifyDataSetChanged();
    }

    public void setShowAddDoc(boolean showAddDoc) {
        mShowAddDoc = showAddDoc;
        notifyDataSetChanged();
    }

    public boolean getShowAddDoc() {
        return mShowAddDoc;
    }

    public void updateSelectedDocuments() {
        notifyDataSetChanged();
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
            String title = "El Quijote de Madrid";
            if (doc.id == 1 ) title = "Title " + doc.id + " largo per no mucho lo suficiente para esto que se vea";
            if (doc.id == 2 ) title = "Title " + doc.id + " largo de cojones me cago en todo puta mierda esto no cabe ni de broma puto usuario";
            if (doc.id == 3 ) title = "Title " + doc.id + " mediano mas asumible un tamaño e";
            if (doc.id == 4 ) title = "ElQuijote";
            if (doc.id == 5 ) title = "ElQuijotedelamancha2";
            if (doc.id == 6 ) title = "ElQuijotedelamancha2ElQuijotedelamancha2";
            DocItemUtils.resolveTitleSize(mContext, title, docHolder.title);
            docHolder.title.setText(title);
            docHolder.words.setText("1500 words");
            docHolder.workTime.setText("40 minutes");
            docHolder.editTime.setText("1999/12/31 00:00");
            docHolder.itemView.setSelected(mSelectedDocumentsIds.contains(doc.id));
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
