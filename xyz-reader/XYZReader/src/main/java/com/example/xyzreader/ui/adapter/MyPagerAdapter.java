package com.example.xyzreader.ui.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.xyzreader.ui.ArticleDetailActivity;
import com.example.xyzreader.ui.ArticleDetailFragment;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    private final ArticleDetailActivity articleDetailActivity;
    private final Cursor mCursor;
    private final int mStartingPosition;

    public MyPagerAdapter(ArticleDetailActivity articleDetailActivity, FragmentManager fm, Cursor cursor, int startingPosition) {
        super(fm);
        this.articleDetailActivity = articleDetailActivity;
        this.mCursor = cursor;
        this.mStartingPosition = startingPosition;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        articleDetailActivity.setCurrentDetailsFragment((ArticleDetailFragment) object);
    }

    @Override
    public Fragment getItem(int position) {
        mCursor.moveToPosition(position);
        return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleDetailActivity.COL_ARTICLE_ID), position, mStartingPosition);
    }

    @Override
    public int getCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
