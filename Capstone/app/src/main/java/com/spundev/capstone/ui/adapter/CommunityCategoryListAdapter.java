package com.spundev.capstone.ui.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.spundev.capstone.R;
import com.spundev.capstone.model.firestore.CategoryFirestore;

import io.fabric.sdk.android.Fabric;

public class CommunityCategoryListAdapter extends FirestoreRecyclerAdapter<CategoryFirestore, CommunityCategoryListAdapter.CommunityCategoryListAdapterViewHolder> {

    // Click handlers
    private final CommunityCategoryListAdapterOnClickHandler adapterClickHandler;

    public interface CommunityCategoryListAdapterOnClickHandler {
        void onCategorySelected(CategoryFirestore category);
    }


    public CommunityCategoryListAdapter(@NonNull FirestoreRecyclerOptions<CategoryFirestore> options, CommunityCategoryListAdapterOnClickHandler ch) {
        super(options);
        adapterClickHandler = ch;
    }

    @Override
    protected void onBindViewHolder(@NonNull CommunityCategoryListAdapterViewHolder holder, int position, @NonNull CategoryFirestore model) {
        holder.nameTextView.setText(model.getName());
        if (!TextUtils.isEmpty(model.getName())) {
            holder.iconLetterTextView.setText(String.valueOf(model.getName().charAt(0)).toUpperCase());
            holder.setCircleColor(model.getColor());
        } else {
            holder.iconLetterTextView.setText("-");
        }
    }

    @NonNull
    @Override
    public CommunityCategoryListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup group, int viewType) {
        // Create a new instance of the ViewHolder, in this case we are using a custom
        // layout called R.layout.list_item_category_legacy for each item
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.list_item_category_community, group, false);

        return new CommunityCategoryListAdapterViewHolder(view);
    }

    class CommunityCategoryListAdapterViewHolder extends RecyclerView.ViewHolder {

        final TextView nameTextView;
        final TextView iconLetterTextView;
        final ImageView categoryCircleImageView;

        CommunityCategoryListAdapterViewHolder(View itemView) {
            super(itemView);
            // Category name
            nameTextView = itemView.findViewById(R.id.name_textView);
            iconLetterTextView = itemView.findViewById(R.id.icon_letter_textView);
            categoryCircleImageView = itemView.findViewById(R.id.category_circle_imageView);

            // Category view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterClickHandler != null) {
                        int adapterPosition = getAdapterPosition();
                        CategoryFirestore selectedCategory = getItem(adapterPosition);
                        adapterClickHandler.onCategorySelected(selectedCategory);
                    }
                }
            });
        }

        void setCircleColor(String circleColor) {
            if (!TextUtils.isEmpty(circleColor)) {
                try {
                    int categoryColor = Color.parseColor(circleColor);
                    categoryCircleImageView.setColorFilter(categoryColor);
                } catch (Exception e) {
                    Log.e("TAG", "setCircleColor: ", e);
                    if (Fabric.isInitialized()) {
                        Crashlytics.log(circleColor);
                        Crashlytics.logException(e);
                    }
                }
            }
        }
    }
}
