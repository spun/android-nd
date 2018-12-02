package com.spundev.bakingtime.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.spundev.bakingtime.RecipeDetailIngredientsFragment;
import com.spundev.bakingtime.RecipeDetailStepsFragment;

/**
 * Created by spundev.
 */

public class DetailsPagerAdapter extends FragmentPagerAdapter {

    private final int recipeId;

    public DetailsPagerAdapter(int recipeId, FragmentManager fm) {
        super(fm);
        this.recipeId = recipeId;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                // Bundle
                Bundle args = new Bundle();
                args.putInt(RecipeDetailStepsFragment.SELECTED_RECIPE, recipeId);
                // steps fragment
                RecipeDetailStepsFragment recipeStepsFragment = new RecipeDetailStepsFragment();
                recipeStepsFragment.setArguments(args);
                return recipeStepsFragment;
            }
            case 1: {
                // Bundle
                Bundle args = new Bundle();
                args.putInt(RecipeDetailIngredientsFragment.SELECTED_RECIPE, recipeId);
                // ingredients fragment
                RecipeDetailIngredientsFragment recipeIngredientsFragment = new RecipeDetailIngredientsFragment();
                recipeIngredientsFragment.setArguments(args);
                return recipeIngredientsFragment;
            }
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Steps";
            case 1:
                return "Ingredients";
            default:
                return null;
        }
    }
}
