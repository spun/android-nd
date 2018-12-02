package com.example.xyzreader.ui.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;

import static com.example.xyzreader.ui.ArticleDetailFragment.COL_PARAGRAPH_PARAGRAPH;

public class ArticleParagraphsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_PARAGRAPH = 1;

    // Header article info
    private String header_date;
    private String header_author;

    private Cursor mCursor;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_TYPE_HEADER) {
            View view = layoutInflater.inflate(R.layout.list_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.list_item_paragraph, parent, false);
            return new ParagraphViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ITEM_TYPE_HEADER) {
            ((HeaderViewHolder) holder).author.setText(header_author);
            ((HeaderViewHolder) holder).date.setText(header_date);
        } else {
            mCursor.moveToPosition(position);
            ((ParagraphViewHolder) holder).paragraphTextView.setText(getParagraph(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_HEADER;
        } else {
            return ITEM_TYPE_PARAGRAPH;
        }
    }

    private String getParagraph(int position) {
        // Cursor position is the recycler position - 1 (HEADER)
        mCursor.moveToPosition(position - 1);
        return mCursor.getString(COL_PARAGRAPH_PARAGRAPH);
    }

    @Override
    public int getItemCount() {
        // Number of paragraphs + header
        if (null == mCursor) return 1;
        return mCursor.getCount() + 1;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public void setHeader(String author, String date) {
        header_author = author;
        header_date = date;
        notifyItemChanged(0);
    }

    // Header ViewHolder
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView date;

        HeaderViewHolder(View view) {
            super(view);
            author = view.findViewById(R.id.author_textView);
            date = view.findViewById(R.id.date_textView);
        }
    }

    // Article paragraph ViewHolder
    class ParagraphViewHolder extends RecyclerView.ViewHolder {
        final TextView paragraphTextView;

        ParagraphViewHolder(View view) {
            super(view);
            paragraphTextView = view.findViewById(R.id.paragraph_textView);
        }
    }
}
