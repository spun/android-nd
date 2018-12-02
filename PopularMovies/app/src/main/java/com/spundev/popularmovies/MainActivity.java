package com.spundev.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.spundev.popularmovies.databinding.ActivityMainBinding;
import com.spundev.popularmovies.model.TMDBMovie;
import com.spundev.popularmovies.util.Theme;

public class MainActivity extends AppCompatActivity implements MainActivityPopularFragment.Callback, MainActivityRatedFragment.Callback, MainActivityFavoritesFragment.Callback, BottomNavigationView.OnNavigationItemSelectedListener {

    private boolean mTwoPane;

    private FragmentManager fragmentManager;
    // Fragments tags
    private static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT_TAG";
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";

    private BottomNavigationView mBottomNavigationView;
    // id for saving the bottom navigation bar on state changes
    private static final String STATE_BOTTOM_NAV = "bottomNav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the selected to the current activity
        Theme.applyTheme(this);

        // Data binding
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailActivityFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        fragmentManager = getSupportFragmentManager();
        // Bottom navigation widget
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);
        // Add listener
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Launcher shortcuts
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = fragmentManager.beginTransaction();

            // If the app was launched from a launcher shortcut
            String launchMode = getIntent().getStringExtra("launch_mode");
            if (launchMode != null) {
                switch (launchMode) {
                    case "popular":
                        // on popular clicked
                        mBottomNavigationView.setSelectedItemId(R.id.popular);
                        break;
                    case "rated":
                        // on rated clicked
                        mBottomNavigationView.setSelectedItemId(R.id.rated);
                        break;
                    case "favorites":
                        // on favorites clicked
                        mBottomNavigationView.setSelectedItemId(R.id.favorites);
                        break;
                    default:
                        // If it was launched by an unknown shortcut, show popular
                        transaction.add(R.id.main_container, new MainActivityPopularFragment()).commit();
                        break;
                }
            } else {
                // Default screen
                transaction.add(R.id.main_container, new MainActivityPopularFragment()).commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BOTTOM_NAV, mBottomNavigationView.getSelectedItemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Settings activity menu
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPopularItemSelected(TMDBMovie movie) {
        showItemSelected(movie);
    }

    @Override
    public void onRatedItemSelected(TMDBMovie movie) {
        showItemSelected(movie);
    }

    @Override
    public void onFavoriteItemSelected(TMDBMovie movie) {
        showItemSelected(movie);
    }

    // Called when a item from one of the lists is selected
    private void showItemSelected(TMDBMovie movie) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_DATA, movie);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);

            fragmentManager.beginTransaction()
                    .replace(R.id.detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, movie);
            startActivity(intent);
        }
    }

    // Bottom navigation listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        if (item.getItemId() == R.id.popular) {
            // on popular clicked
            fragment = new MainActivityPopularFragment();
        } else if (item.getItemId() == R.id.rated) {
            // on rated clicked
            fragment = new MainActivityRatedFragment();
        } else if (item.getItemId() == R.id.favorites) {
            // on favorites clicked
            fragment = new MainActivityFavoritesFragment();
        } else {
            // default
            fragment = new MainActivityPopularFragment();
        }

        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment
        // and commit the transaction
        transaction.replace(R.id.main_container, fragment, MAIN_FRAGMENT_TAG).commit();
        return true;
    }
}
