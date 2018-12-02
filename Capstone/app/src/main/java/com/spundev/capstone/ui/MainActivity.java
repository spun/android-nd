package com.spundev.capstone.ui;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.spundev.capstone.R;
import com.spundev.capstone.ui.adapter.MainActivityPageAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the tabs adapter.
        ViewPager viewPager = findViewById(R.id.viewpager_container);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //FavoritesWidget.updateFavoritesWidgets(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Search config
        SearchManager searchManager = (SearchManager)        getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchActivity.class);
        // Search view
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Settings activity menu
        if (id == R.id.action_settings) {
            SettingsActivity.start(this);
            return true;
        } else if (id == R.id.action_my_account) {
            Intent starter = new Intent(this, MyAccountActivity.class);
            startActivity(starter, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        MainActivityPageAdapter adapter = new MainActivityPageAdapter(getSupportFragmentManager());
        // Add tabs
        adapter.addFragment(new LocalCategoriesFragment(), getString(R.string.tab_local));
        adapter.addFragment(new CommunityCategoriesFragment(), getString(R.string.tab_community));
        viewPager.setAdapter(adapter);
    }
}
