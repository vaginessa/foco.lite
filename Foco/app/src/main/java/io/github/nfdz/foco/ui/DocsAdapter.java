package io.github.nfdz.foco.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
