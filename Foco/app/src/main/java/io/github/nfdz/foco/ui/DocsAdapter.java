package io.github.nfdz.foco.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.nfdz.foco.R;

public class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.ViewHolder> {

    public interface DocsClickHandler {
        void onDocumentClick();
        void onDocumentLongClick();
    }

    private final Context mContext;
    private final DocsClickHandler mHandler;

    public DocsAdapter(@NonNull Context context, @Nullable DocsClickHandler clickHandler) {
        mContext = context;
        mHandler = clickHandler;
    }

    @Override
    public DocsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.doc_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParent = false;
        View view = inflater.inflate(layoutId, parent, shouldAttachToParent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocsAdapter.ViewHolder holder, int position) {
        holder.title.setText("Title etc");
        holder.words.setText("1500");
        holder.time.setText("40 min");
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.doc_item_title) TextView title;
        @BindView(R.id.doc_item_time) TextView time;
        @BindView(R.id.doc_item_words) TextView words;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mHandler != null) mHandler.onDocumentClick();
        }

        @Override
        public boolean onLongClick(View v) {
            if (mHandler != null) mHandler.onDocumentLongClick();
            return true;
        }
    }
}
