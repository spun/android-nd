package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.ui.adapter.ArticleParagraphsAdapter;
import com.example.xyzreader.util.ArticleDateUtils;
import com.example.xyzreader.util.ThemeColorUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;


/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    // Fragment creation arguments
    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_STARTING_ITEM_POSITION = "arg_starting_item_position";
    private static final String ARG_CURRENT_ITEM_POSITION = "arg_current_position";

    private long mArticleId;
    private int mStartingPosition;
    private int mCurrentPosition;
    private boolean mIsTransitioning;

    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mArticleImage;
    private FloatingActionButton mFab;

    private ArticleParagraphsAdapter mArticleParagraphsAdapter;

    // Values to hide the fab (manually when >600dp)
    private int mMaxScrollSize;
    private boolean mIsFabHidden;
    private static final int PERCENTAGE_TO_SHOW_IMAGE = 20;

    // Text to share
    private String mShareText = "";

    // Loader ids
    private static final int ARTICLE_DETAILS_LOADER_ID = 10;
    private static final int ARTICLE_PARAGRAPHS_LOADER_ID = 20;

    // Article columns projection
    private static final String[] ARTICLE_COLUMNS = {
            ItemsContract.Items.TITLE,
            ItemsContract.Items.AUTHOR,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.PHOTO_URL
    };
    // These indices are tied to ARTICLE_COLUMNS. If ARTICLE_COLUMNS changes, these must change.
    private static final int COL_ARTICLE_TITLE = 0;
    private static final int COL_ARTICLE_AUTHOR = 1;
    private static final int COL_ARTICLE_PUBLISHED_DATE = 2;
    private static final int COL_ARTICLE_PHOTO_URL = 3;


    // Paragraph columns projection
    private static final String[] PARAGRAPH_COLUMNS = {
            ItemsContract.Paragraphs.TEXT
    };
    // These indices are tied to PARAGRAPH_COLUMNS. If PARAGRAPH_COLUMNS changes, these must change.
    public static final int COL_PARAGRAPH_PARAGRAPH = 0;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position, int startingPosition) {
        Bundle arguments = new Bundle();
        // Id of the article we are going to show
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ARG_CURRENT_ITEM_POSITION, position);
        arguments.putInt(ARG_STARTING_ITEM_POSITION, startingPosition);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArticleId = getArguments().getLong(ARG_ITEM_ID);
            mStartingPosition = getArguments().getInt(ARG_STARTING_ITEM_POSITION);
            mCurrentPosition = getArguments().getInt(ARG_CURRENT_ITEM_POSITION);
        }
        mIsTransitioning = savedInstanceState == null && mStartingPosition == mCurrentPosition;
    }


    // Callback for the image loading
    private final Callback mImageCallback = new Callback() {
        @Override
        public void onSuccess() {
            // Get the bitmap
            Bitmap bitmap = ((BitmapDrawable) mArticleImage.getDrawable()).getBitmap();
            // Use of the library Palette to get the main colors of the image
            Palette p = Palette.from(bitmap).generate();
            Palette.Swatch mutedSwatch = p.getMutedSwatch();

            // Check that the muted swatch is available
            if (mutedSwatch != null) {
                // We use the muted swatch to change the color of the toolbar, we could use the
                // "dark muted" swatch to change the status bar, but we don't have any guaranty
                // that the colors fir together, so we instead calculate the status bar color from
                // the muted.
                ThemeColorUtils.changeCollapsingToolbarColors(mCollapsingToolbar, mutedSwatch.getRgb());
                // We apply a subtle tint to the image to make it less distracting
                mArticleImage.setColorFilter(ColorUtils.setAlphaComponent(mutedSwatch.getRgb(), 128));
            }
            // Resume the postponed enter transition
            startPostponedEnterTransition();
        }

        @Override
        public void onError() {
            // If we couldn't load the image, resume the postponed enter transition
            startPostponedEnterTransition();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        // Toolbar
        Toolbar toolbar = mRootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // Toolbar back arrow
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // CollapsingToolbar
        mCollapsingToolbar = mRootView.findViewById(R.id.collapsing_toolbar);
        // Article image inside the CollapsingToolbar
        mArticleImage = mRootView.findViewById(R.id.photo);

        // Transition name for the shared element
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = String.valueOf(mArticleId);
            mArticleImage.setTransitionName(transitionName);
        }

        // We use a RecyclerView to show the text of the article
        RecyclerView paragraphsRecyclerView = mRootView.findViewById(R.id.body_text_recyclerView);
        // Layout manager
        paragraphsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        // Adapter
        mArticleParagraphsAdapter = new ArticleParagraphsAdapter();
        paragraphsRecyclerView.setAdapter(mArticleParagraphsAdapter);

        mFab = mRootView.findViewById(R.id.share_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable shareDrawable = mFab.getDrawable();
                    if (shareDrawable instanceof AnimatedVectorDrawable) {
                        ((AnimatedVectorDrawable) mFab.getDrawable()).start();
                    }
                }
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(mShareText)
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        // Hide fab on scroll (we need to do it manually if we are showing the > 600dp layout)
        AppBarLayout appbar = mRootView.findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this);

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Load the data of the article
        getLoaderManager().initLoader(ARTICLE_DETAILS_LOADER_ID, null, this);
        // Load the paragraphs of the article
        getLoaderManager().initLoader(ARTICLE_PARAGRAPHS_LOADER_ID, null, this);
    }

    @Override
    public void startPostponedEnterTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mCurrentPosition == mStartingPosition) {
                mArticleImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onPreDraw() {
                        mArticleImage.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (getActivity() != null) {
                            getActivity().startPostponedEnterTransition();
                        }
                        return true;
                    }
                });
            }
        }
    }

    @Nullable
    ImageView getArticleImage() {
        // Check if the image is visible
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mArticleImage)) {
            return mArticleImage;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == ARTICLE_DETAILS_LOADER_ID) {
            // Uri the article
            Uri articlesUri = ItemsContract.Items.buildItemUri(mArticleId);

            // Return cursor loader
            return new CursorLoader(getActivity(),
                    articlesUri,
                    ARTICLE_COLUMNS,
                    null,
                    null,
                    ItemsContract.Items.DEFAULT_SORT);

        } else if (id == ARTICLE_PARAGRAPHS_LOADER_ID) {
            // Uri for all the paragraphs in the article
            Uri articleParagraphsUri = ItemsContract.Paragraphs.buildDirUri(mArticleId);

            // Return cursor loader
            return new CursorLoader(getActivity(),
                    articleParagraphsUri,
                    PARAGRAPH_COLUMNS,
                    null,
                    null,
                    ItemsContract.Paragraphs.DEFAULT_SORT);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int loaderId = loader.getId();
        if (loaderId == ARTICLE_DETAILS_LOADER_ID) {
            if (data != null && data.moveToFirst()) {
                // Article data
                String articleTitle = data.getString(COL_ARTICLE_TITLE);
                String articleImageUrl = data.getString(COL_ARTICLE_PHOTO_URL);
                String articleAuthor = data.getString(COL_ARTICLE_AUTHOR);
                String articlePublishedDate = data.getString(COL_ARTICLE_PUBLISHED_DATE);

                // Set title
                mCollapsingToolbar.setTitle(articleTitle);
                mShareText = articleTitle;

                // Parse published data
                String publishedDate = ArticleDateUtils.parsePublishedDate(articlePublishedDate);

                // Set header with the author and date
                mArticleParagraphsAdapter.setHeader(articleAuthor, publishedDate);

                // Load article image
                RequestCreator albumImageRequest = Picasso.with(getActivity()).load(articleImageUrl);
                if (mIsTransitioning) {
                    albumImageRequest.noFade();
                }
                albumImageRequest.into(mArticleImage, mImageCallback);

                // Set alpha to 0 to and fade in
                /*if (mRootView.getVisibility() != View.VISIBLE) {
                    mRootView.setAlpha(0);
                    mRootView.setVisibility(View.VISIBLE);
                    mRootView.animate().alpha(1);
                }*/
            }
        } else if (loaderId == ARTICLE_PARAGRAPHS_LOADER_ID) {
            mArticleParagraphsAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int loaderId = loader.getId();
        if (loaderId == ARTICLE_PARAGRAPHS_LOADER_ID) {
            mArticleParagraphsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int currentScrollPercentage = (Math.abs(verticalOffset)) * 100
                / mMaxScrollSize;

        if (currentScrollPercentage >= PERCENTAGE_TO_SHOW_IMAGE) {
            if (!mIsFabHidden) {
                mIsFabHidden = true;
                ViewCompat.animate(mFab).scaleY(0).scaleX(0).start();
            }
        }

        if (currentScrollPercentage < PERCENTAGE_TO_SHOW_IMAGE) {
            if (mIsFabHidden) {
                mIsFabHidden = false;
                ViewCompat.animate(mFab).scaleY(1).scaleX(1).start();
            }
        }
    }
}
