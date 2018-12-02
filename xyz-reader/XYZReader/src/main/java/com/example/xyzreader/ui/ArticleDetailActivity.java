package com.example.xyzreader.ui;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.ui.adapter.MyPagerAdapter;

import java.util.List;
import java.util.Map;

import static com.example.xyzreader.ui.ArticleListActivity.EXTRA_CURRENT_ITEM_POSITION;
import static com.example.xyzreader.ui.ArticleListActivity.EXTRA_STARTING_ITEM_POSITION;


/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private static final int ARTICLES_IDS_LOADER_ID = 10;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private Cursor mCursor;

    private ArticleDetailFragment mCurrentDetailsFragment;
    private int mCurrentPosition;
    private int mStartingPosition;
    private boolean mIsReturning;

    // Article columns projection
    private static final String[] ARTICLES_COLUMNS = {
            ItemsContract.Items._ID
    };
    // These indices are tied to ARTICLES_COLUMNS. If ARTICLES_COLUMNS changes, these must change.
    public static final int COL_ARTICLE_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Delay enter transition until we load the shared image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            setEnterSharedElementCallback(getSharedElementCallback());
        }

        // Get starting position
        mStartingPosition = getIntent().getIntExtra(EXTRA_STARTING_ITEM_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        // Articles pager
        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });

        // Initialize loader
        getSupportLoaderManager().initLoader(ARTICLES_IDS_LOADER_ID, null, this);

        // Detail toolbar bug fix "https://issuetracker.google.com/issues/37054303#comment25"
        ViewCompat.setOnApplyWindowInsetsListener(mPager, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                insets = ViewCompat.onApplyWindowInsets(v, insets);
                if (insets.isConsumed()) {
                    return insets;
                }

                boolean consumed = false;
                for (int i = 0, count = mPager.getChildCount(); i < count; i++) {
                    ViewCompat.dispatchApplyWindowInsets(mPager.getChildAt(i), insets);
                    if (insets.isConsumed()) {
                        consumed = true;
                    }
                }
                return consumed ? insets.consumeSystemWindowInsets() : insets;
            }
        });
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private SharedElementCallback getSharedElementCallback() {

        SharedElementCallback mCallback = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mIsReturning) {
                        ImageView sharedElement = mCurrentDetailsFragment.getArticleImage();
                        if (sharedElement == null) {
                            // If shared element is null, then it has been scrolled off screen and
                            // no longer visible. In this case we cancel the shared element transition by
                            // removing the shared element from the shared elements map.
                            names.clear();
                            sharedElements.clear();
                        } else if (mStartingPosition != mCurrentPosition) {
                            // If the user has swiped to a different ViewPager page, then we need to
                            // remove the old shared element and replace it with the new shared element
                            // that should be transitioned instead.
                            names.clear();
                            names.add(sharedElement.getTransitionName());
                            sharedElements.clear();
                            sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                        }
                    }
                }
            };
        }
        return mCallback;
    }


    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_ITEM_POSITION, mStartingPosition);
        data.putExtra(EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Loader of all articles
        if (id == ARTICLES_IDS_LOADER_ID) {
            // Uri for all the articles
            Uri articlesUri = ItemsContract.Items.buildDirUri();

            // Return cursor loader
            return new CursorLoader(this,
                    articlesUri,
                    ARTICLES_COLUMNS,
                    null,
                    null,
                    ItemsContract.Items.DEFAULT_SORT);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == ARTICLES_IDS_LOADER_ID) {
            mCursor = data;
            // Now that we have the cursor, we must update the adapter.
            // The adapter is already using our cursor, so creating a new adapter for each
            // "onLoadFinished" may look unnecessary when we have "notifyDataSetChanged",
            // but it seems that the loaded views are not getting updated.
            // The problem and the solution alternative to this creation of new adapters
            // are explained here: https://stackoverflow.com/a/8024557
            // TODO: Implement solution from stackoverflow
            mPagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), mCursor, mStartingPosition);
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(mCurrentPosition);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Update cursor
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    public void setCurrentDetailsFragment(ArticleDetailFragment currentDetailsFragment) {
        this.mCurrentDetailsFragment = currentDetailsFragment;
    }
}
