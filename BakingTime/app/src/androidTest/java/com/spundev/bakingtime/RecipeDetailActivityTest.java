package com.spundev.bakingtime;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.spundev.bakingtime.provider.DatabaseHelper.DATABASE_NAME;
import static com.spundev.bakingtime.sync.RecipesSyncUtils.insertIntoDatabase;
import static org.hamcrest.core.IsNot.not;


@RunWith(AndroidJUnit4.class)
public class RecipeDetailActivityTest {

    @Rule
    public final ActivityTestRule<RecipeDetailActivity> mActivityTestRule = new ActivityTestRule<>(RecipeDetailActivity.class, false, false);

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
        Intent intent = new Intent();
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, 1);
        mActivityTestRule.launchActivity(intent);
    }

    @Test
    public void recipeListContentTest() {
        // Check the toolbar title on the recipe details activity
        onView(isAssignableFrom(Toolbar.class))
                .check(matches(ToolbarMatcher.withToolbarTitle("recipe test")));

        // Check the if the steps_recyclerView is visible (default tab)
        onView(withId(R.id.steps_recyclerView)).check(matches(isDisplayed()));

        // Check the if the ingredients_recyclerView is not visible
        onView(withId(R.id.ingredients_recyclerView)).check(matches(not(isDisplayed())));
    }


    @Test
    public void switchTabTest() {

        onView((withId(R.id.steps_recyclerView)))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));

        try {
            // Swipe still has an animation ¿?
            Thread.sleep(300);

            // Check the if the ingredients_recyclerView is visible
            onView(withId(R.id.ingredients_recyclerView)).check(matches(isDisplayed()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void openStepTest() {
        // Click on the first item of the recycler view
        onView((withId(R.id.steps_recyclerView))).
                perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check the step short description
        onView((withId(R.id.step_detail_short_description)))
                .check(matches(withText("Step 1 short description")));
    }

    @AfterClass
    public static void DeleteDatabase() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DATABASE_NAME);
    }
}