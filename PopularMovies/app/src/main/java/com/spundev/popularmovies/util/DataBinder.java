package com.spundev.popularmovies.util;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.spundev.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Created by spundev.
 */

@SuppressWarnings("WeakerAccess")
public class DataBinder {
    // Custom attribute that fetches the indicated url using picasso
    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        Context context = imageView.getContext();
        Picasso.with(context).load(url)
                .error(R.drawable.ic_error_outline_24dp)
                .placeholder(R.drawable.placeholder)
                .into(imageView);
    }
}
