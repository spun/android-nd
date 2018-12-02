package com.spundev.capstone.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.spundev.capstone.R;

import java.util.ArrayList;
import java.util.List;

/* List of colors for the new category dialog */
public class CategoryColorsAdapter extends RecyclerView.Adapter<CategoryColorsAdapter.CategoryColorViewHolder> {

    private final List<Integer> availableCategoryColors = new ArrayList<>();
    private int selectedColorPosition = 0;

    public CategoryColorsAdapter(Context context) {

        int[] colorsArray = context.getResources().getIntArray(R.array.category_colors);
        for (int color : colorsArray) {
            availableCategoryColors.add(color);
        }
    }

    @NonNull
    @Override
    public CategoryColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category_color, parent, false);
        return new CategoryColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryColorViewHolder holder, int position) {
        int colorValue = availableCategoryColors.get(position);
        holder.circleImageView.setColorFilter(colorValue);
        if (position == selectedColorPosition) {
            holder.checkImageView.setVisibility(View.VISIBLE);
        } else {
            holder.checkImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return availableCategoryColors.size();
    }

    public int getSelectedColor() {
        return availableCategoryColors.get(selectedColorPosition);
    }

    public class CategoryColorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Each color has a circle
        final ImageView circleImageView;
        // The selected color shows a checkmark inside the circle
        final ImageView checkImageView;

        CategoryColorViewHolder(final View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.category_color_circle_imageView);
            checkImageView = itemView.findViewById(R.id.category_color_check_imageView);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (selectedColorPosition != RecyclerView.NO_POSITION) {
                int oldSelectedItemPosition = selectedColorPosition;
                selectedColorPosition = RecyclerView.NO_POSITION;
                notifyItemChanged(oldSelectedItemPosition);
            }
            selectedColorPosition = getAdapterPosition();
            notifyItemChanged(selectedColorPosition);
        }
    }
}
