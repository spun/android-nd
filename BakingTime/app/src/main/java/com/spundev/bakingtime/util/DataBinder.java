package com.spundev.bakingtime.util;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.spundev.bakingtime.R;

/**
 * Created by spundev.
 */

@SuppressWarnings("WeakerAccess")
public class DataBinder {

    // Custom attribute that fetches the indicated url using glide
    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        Context context = imageView.getContext();

        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.recipe_img_placeholder)
                    .into(imageView);
        }
    }
}