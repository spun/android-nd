package com.spundev.bakingtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.Toolbar;

import com.spundev.bakingtime.matchers.ToolbarMatcher;
import com.spundev.bakingtime.model.Ingredient;
import com.spundev.bakingtime.model.Recipe;
import com.spundev.bakingtime.model.Step;
import com.spundev.bakingtime.provider.DatabaseHelper;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.spundev.bakingtime.provider.DatabaseHelper.DATABASE_NAME;
import static com.spundev.bakingtime.sync.RecipesSyncUtils.insertIntoDatabase;
import static org.hamcrest.core.IsNot.not;

public class StepDetailActivityTest {

    @Rule
    public final ActivityTestRule<StepDetailActivity> mActivityTestRule = new ActivityTestRule<>(StepDetailActivity.class, false, false);

    @BeforeClass
    public static void PrepareDatabase() {

        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        appContext.deleteDatabase(DATABASE_NAME);

        new DatabaseHelper(appContext);

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(1, "ingredient 1", 1, "CUP", true, 1));

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(1, "Step 1 short description", "Step 1 description", "", ""));
        steps.add(new Step(2, "Step 2 short description", "Step 2 description", "", ""));

        List<Recipe> recipes = new ArrayList<>();
        recipes.add(new Recipe(1, "recipe test", 5, "", 2, 1));
        recipes.get(0).setIngredients(ingredients);
        recipes.get(0).setSteps(steps);

        insertIntoDatabase(appContext, recipes);
    }

    @Before
    public void LaunchActivity() {
        //launches the test activity once we have the database ready
        Bundle args = new Bundle();
        args.putInt(StepDetailActivityFragment.SELECTED_RECIPE_EXTRA, 1);
        args.putInt(StepDetailActivityFragment.SELECTED_STEP_EXTRA, 1);

        Intent intent = new Intent();
        intent.putExtras(args);

        mActivityTestRule.launchActivity(intent);
    }

    @Test
    public void recipeListContentTest() {
        // Check the toolbar title on the step details activity
        onView(isAssignableFrom(Toolbar.class))
                .check(matches(ToolbarMatcher.withToolbarTitle("Step details")));

        // Check the step short description
        onView((withId(R.id.step_detail_short_description)))
                .check(matches(withText("Step 1 short description")));

        // Check the step description
        onView((withId(R.id.step_detail_description)))
                .check(matches(withText("Step 1 description")));
    }

    @Test
    public void nextStepButtonTest() {
        // Check the step description
        onView((withId(R.id.goNextButton)))
                .check(matches(isEnabled()))
                .perform(click())
                .check(matches(not(isEnabled())));

        // Check the step short description
        onView((withId(R.id.step_detail_short_description)))
                .check(matches(withText("Step 2 short description")));

        // Check the step description
        onView((withId(R.id.step_detail_description)))
                .check(matches(withText("Step 2 description")));
    }

    @Test
    public void backStepButtonTest() {

        // Check the step description
        onView((withId(R.id.goBackButton)))
                .check(matches(not(isEnabled())));

        // Check the step description
        onView((withId(R.id.goNextButton)))
                .perform(click());

        // Check the step description
        onView((withId(R.id.goBackButton)))
                .check(matches(isEnabled()))
                .perform(click())
                .check(matches(not(isEnabled())));

        // Check the step short description
        onView((withId(R.id.step_detail_short_description)))
                .check(matches(withText("Step 1 short description")));

        // Check the step description
        onView((withId(R.id.step_detail_description)))
                .check(matches(withText("Step 1 description")));
    }


    @AfterClass
    public static void DeleteDatabase() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DATABASE_NAME);
    }
}