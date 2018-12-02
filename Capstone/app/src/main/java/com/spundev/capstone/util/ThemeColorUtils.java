package com.spundev.capstone.util;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.Toolbar;

public class ThemeColorUtils {

    public static void changeActivityColors(Activity activity, Toolbar toolbar, @ColorInt int primaryColor) {
        // Set toolbar color
        toolbar.setBackgroundColor(primaryColor);
        // Set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int darkColor = adjustColorForStatusBar(primaryColor);
            activity.getWindow().setStatusBarColor(darkColor);
        }
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
}
