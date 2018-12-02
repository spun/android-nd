package com.spundev.capstone.util;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * A {@link RecyclerView.ItemAnimator} which allows disabling move animations. RecyclerView
 * does not like animating item height changes. {@link android.transition.ChangeBounds} allows
 * this but in order to simultaneously collapse one item and expand another, we need to run the
 * Transition on the entire RecyclerView. As such it attempts to move views around. This
 * custom item animator allows us to stop RecyclerView from trying to handle this for us while
 * the transition is running.
 */
public class CardAnimator extends DefaultItemAnimator {

    private boolean animateMoves = false;

    public CardAnimator() {
        super();
    }

    public void setAnimateMoves(boolean animateMoves) {
        this.animateMoves = animateMoves;
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        if (!animateMoves) {
            dispatchMoveFinished(holder);
            return false;
        }
        return super.animateMove(holder, fromX, fromY, toX, toY);
    }
}
