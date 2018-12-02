package com.spundev.bakingtime;


import android.content.Context;
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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.spundev.bakingtime.provider.DatabaseHelper.DATABASE_NAME;
import static com.spundev.bakingtime.sync.RecipesSyncUtils.insertIntoDatabase;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, false, false);

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
        mActivityTestRule.launchActivity(null);
    }

    @Test
    public void recipeListContentTest() {
        // Check the if the recipes_recyclerView is visible (hidden when empty)
        onView(withId(R.id.recipes_recyclerView)).check(matches(isDisplayed()));

        // Check if the recipe name is correct
        onView((withId(R.id.name_textView))).check(matches(withText("recipe test")));

        // Check recipe numbers
        onView((withId(R.id.ingredients_textView))).check(matches(withText("1")));
        onView((withId(R.id.steps_textView))).check(matches(withText("2")));
        onView((withId(R.id.servings_textView))).check(matches(withText("5")));
    }

    @Test
    public void openRecipeTest() {
        // Click on the first item of the recycler view
        onView((withId(R.id.recipes_recyclerView))).
                perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check the toolbar title on the recipe details activity
        onView(isAssignableFrom(Toolbar.class))
                .check(matches(ToolbarMatcher.withToolbarTitle("recipe test")));
    }

    @AfterClass
    public static void DeleteDatabase() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.deleteDatabase(DATABASE_NAME);
    }
}
