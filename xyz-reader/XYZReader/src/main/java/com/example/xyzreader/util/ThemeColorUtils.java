package com.example.xyzreader.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.graphics.ColorUtils;

import com.example.xyzreader.R;

import static android.graphics.drawable.GradientDrawable.RECTANGLE;

public class ThemeColorUtils {

    public static void changeCollapsingToolbarColors(CollapsingToolbarLayout collapsingToolbar, @ColorInt int primaryColor) {
        collapsingToolbar.setBackgroundColor(primaryColor);
        collapsingToolbar.setContentScrimColor(primaryColor);
        // Calculate dark color
        int darkColor = adjustColorForStatusBar(primaryColor);
        collapsingToolbar.setStatusBarScrimColor(darkColor);
    }

    @ColorInt
    private static int adjustColorForStatusBar(@ColorInt int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        // darken the color
        float lightness = hsl[2] * 0.8f;
        // constrain lightness to be within [0–1]
        lightness = Math.max(0f, Math.min(1f, lightness));
        hsl[2] = lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    public static GradientDrawable generatePlaceholderWithRatio(Context context, float ratio) {
        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setShape(RECTANGLE);
        shapeDrawable.setColor(context.getResources().getColor(R.color.colorPlaceholder));
        shapeDrawable.setSize(1,  (int) (1 / ratio));
        return shapeDrawable;
    }
}