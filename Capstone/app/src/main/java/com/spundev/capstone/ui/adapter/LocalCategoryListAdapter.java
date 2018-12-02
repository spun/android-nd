package com.spundev.capstone.ui.adapter;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spundev.capstone.R;
import com.spundev.capstone.model.Category;

import java.util.List;

public class LocalCategoryListAdapter extends RecyclerView.Adapter<LocalCategoryListAdapter.CategoryViewHolder> {

    // Click handlers
    private final CategoryListAdapterHandler adapterClickHandler;

    public interface CategoryListAdapterHandler {
        void onCategorySelected(Category category);

        void onCategoryLongPress(Category category);
    }

    private final LayoutInflater layoutInflater;
    private List<Category> categoriesList; // Cached copy of categories

    public LocalCategoryListAdapter(Context context, CategoryListAdapterHandler ch) {
        this.layoutInflater = LayoutInflater.from(context);
        this.adapterClickHandler = ch;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.list_item_category_local, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category current = getItem(position);
        holder.nameTextView.setText(current.getName());
        if (!TextUtils.isEmpty(current.getName())) {
            holder.iconLetterTextView.setText(String.valueOf(current.getName().charAt(0)).toUpperCase());
            holder.setCircleColor(current.getColor());
        } else {
            holder.iconLetterTextView.setText("-");
        }
    }

    public void setCategories(List<Category> categories) {
        categoriesList = categories;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {

        return getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        if (categoriesList != null)
            return categoriesList.size();
        else return 0;
    }

    private Category getItem(int position) {
        return categoriesList.get(position);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView nameTextView;
        final TextView iconLetterTextView;
        final ImageView categoryCircleImageView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_textView);
            iconLetterTextView = itemView.findViewById(R.id.icon_letter_textView);
            categoryCircleImageView = itemView.findViewById(R.id.category_circle_imageView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (adapterClickHandler != null) {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Category selectedCategory = getItem(adapterPosition);
                    adapterClickHandler.onCategorySelected(selectedCategory);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (adapterClickHandler != null) {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Category selectedCategory = getItem(adapterPosition);
                    adapterClickHandler.onCategoryLongPress(selectedCategory);
                    return true;
                }
            }
            return false;
        }


        void setCircleColor(@ColorInt int circleColor) {
            categoryCircleImageView.setColorFilter(circleColor);
        }
    }
}
