package com.spundev.bakingtime.matchers;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ToolbarMatcher {
    public static Matcher<View> withToolbarTitle(final CharSequence expectedTitle) {
        return new BoundedMatcher<View, Toolbar>(Toolbar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with toolbar title: " + expectedTitle);
            }

            @Override
            protected boolean matchesSafely(Toolbar toolbar) {
                return expectedTitle.equals(toolbar.getTitle());
            }
        };
    }
}
