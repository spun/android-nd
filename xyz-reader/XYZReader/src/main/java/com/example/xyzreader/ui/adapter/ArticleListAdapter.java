package com.example.xyzreader.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.ArticleListActivity;
import com.example.xyzreader.util.ArticleDateUtils;
import com.example.xyzreader.util.ThemeColorUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private final ArticleListAdapterOnClickHandler mClickHandler;

    public interface ArticleListAdapterOnClickHandler {
        void onClick(View view, int itemPosition);
    }

    public ArticleListAdapter(Context context, ArticleListAdapterOnClickHandler ch) {
        this.mContext = context;
        this.mClickHandler = ch;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleListActivity.COL_ARTICLE_ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        // Title
        holder.titleView.setText(mCursor.getString(ArticleListActivity.COL_ARTICLE_TITLE));
        // Date
        String publishedDateString = ArticleDateUtils.parsePublishedDate(mCursor.getString(ArticleListActivity.COL_ARTICLE_PUBLISHED_DATE));
        holder.subtitleView.setText(publishedDateString);
        // Placeholder
        float ratio = mCursor.getFloat(ArticleListActivity.COL_ARTICLE_ASPECT_RATIO);
        // Image
        Picasso.with(mContext)
                .load(mCursor.getString(ArticleListActivity.COL_ARTICLE_THUMB_URL))
                .placeholder(ThemeColorUtils.generatePlaceholderWithRatio(mContext, ratio))
                .into(holder.thumbnailView, new Callback() {

                    @Override
                    public void onSuccess() {
                        holder.thumbnailView.setAlpha(0f);

                        Bitmap bitmap = ((BitmapDrawable) holder.thumbnailView.getDrawable()).getBitmap();

                        Palette p = Palette.from(bitmap).generate();
                        Palette.Swatch darkMutedSwatch = p.getDarkMutedSwatch();

                        // Load default colors
                        int backgroundColor = ContextCompat.getColor(mContext, android.R.color.white);
                        int titleTextColor = ContextCompat.getColor(mContext, android.R.color.black);
                        int bodyTextColor = ContextCompat.getColor(mContext, android.R.color.black);

                        // Check that the Vibrant swatch is available
                        if (darkMutedSwatch != null) {
                            backgroundColor = darkMutedSwatch.getRgb();
                            titleTextColor = ContextCompat.getColor(mContext, android.R.color.white);
                            bodyTextColor = darkMutedSwatch.getBodyTextColor();
                        }

                        holder.itemView.setBackgroundColor(backgroundColor);
                        holder.titleView.setTextColor(titleTextColor);
                        holder.subtitleView.setTextColor(bodyTextColor);

                        int duration = mContext.getResources().getInteger(R.integer.anim_duration_slow);
                        holder.thumbnailView.animate()
                                .alpha(1f)
                                .setDuration(duration)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.thumbnailView.setAlpha(1f);
                                    }
                                });
                    }

                    @Override
                    public void onError() {

                    }
                });

        long articleId = mCursor.getLong(ArticleListActivity.COL_ARTICLE_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.thumbnailView.setTransitionName(String.valueOf(articleId));
        }
        holder.thumbnailView.setTag(String.valueOf(articleId));
        holder.itemPosition = holder.getAdapterPosition();
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView thumbnailView;
        final TextView titleView;
        final TextView subtitleView;
        private int itemPosition;

        ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickHandler != null) {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mClickHandler.onClick(thumbnailView, itemPosition);
                }
            }
        }
    }
}
