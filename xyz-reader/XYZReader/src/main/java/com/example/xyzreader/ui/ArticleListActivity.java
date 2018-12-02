package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.ui.adapter.ArticleListAdapter;


/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Selected item extra
    public static final String EXTRA_STARTING_ITEM_POSITION = "extra_starting_item_position";
    // Current item position extra (after swiping)
    static final String EXTRA_CURRENT_ITEM_POSITION = "extra_current_item_position";

    // Loader id
    private static final int ARTICLES_LIST_LOADER_ID = 10;

    // Pull to refresh layout
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsRefreshing = false;

    // Items recycler view
    private RecyclerView mRecyclerView;
    private ArticleListAdapter mArticlesAdapter;

    // Columns projection
    private static final String[] ARTICLES_COLUMNS = {
            ItemsContract.Items._ID,
            ItemsContract.Items.TITLE,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.THUMB_URL,
            ItemsContract.Items.ASPECT_RATIO

    };
    // These indices are tied to ARTICLES_COLUMNS. If ARTICLES_COLUMNS changes, these must change.
    public static final int COL_ARTICLE_ID = 0;
    public static final int COL_ARTICLE_TITLE = 1;
    public static final int COL_ARTICLE_PUBLISHED_DATE = 2;
    public static final int COL_ARTICLE_THUMB_URL = 3;
    public static final int COL_ARTICLE_ASPECT_RATIO = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove default title to show the app logo
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        // Pull to refresh
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        // Articles recyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        // Layout manager
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        // Adapter
        mArticlesAdapter = new ArticleListAdapter(this, new ArticleListAdapter.ArticleListAdapterOnClickHandler() {
            @Override
            public void onClick(View view, int itemPosition) {
                // New intent with the position of the image and the article id
                Intent intent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
                intent.putExtra(EXTRA_STARTING_ITEM_POSITION, itemPosition);

                // Launch activity
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // With shared element
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                            ArticleListActivity.this,
                            view,
                            view.getTransitionName()).toBundle());
                } else {
                    // Without shared element
                    startActivity(intent);
                }
            }
        });
        mRecyclerView.setAdapter(mArticlesAdapter);

        if (savedInstanceState == null) {
            // Fetch new items from remote source
            refresh();
        }

        // Initialize articles loader
        getSupportLoaderManager().initLoader(ARTICLES_LIST_LOADER_ID, null, this);
    }

    private void refresh() {
        // Start an IntentService to fetch articles
        startService(new Intent(this, UpdaterService.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Resister receiver that listens to updates from the sync IntentService (UpdaterService)
        registerReceiver(mRefreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister receiver
        unregisterReceiver(mRefreshingReceiver);
    }

    // Broadcast receiver that listens for updates from the sync IntentService (UpdaterService)
    private final BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        // Show/hide sync progress view
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        // We check if the item from which we are coming back is the same that we selected to
        // launch the details activity
        Bundle reenterState = new Bundle(data.getExtras());
        int startingPosition = reenterState.getInt(EXTRA_STARTING_ITEM_POSITION);
        int currentPosition = reenterState.getInt(EXTRA_CURRENT_ITEM_POSITION);
        if (startingPosition != currentPosition) {
            // If it wasn't, we move the recycler view to make sure that the item is now
            // visible
            mRecyclerView.scrollToPosition(currentPosition);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == ARTICLES_LIST_LOADER_ID) {
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
        if (loaderId == ARTICLES_LIST_LOADER_ID) {
            mArticlesAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == ARTICLES_LIST_LOADER_ID) {
            mArticlesAdapter.swapCursor(null);
        }
    }
}
