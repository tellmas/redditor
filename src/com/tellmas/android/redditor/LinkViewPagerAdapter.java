package com.tellmas.android.redditor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

/**
 *
 */
public class LinkViewPagerAdapter extends FragmentPagerAdapter {

    protected static int NUM_FRAGMENTS = 3;
    private Redditor activity;


    /**
     * @param fm
     */
    public LinkViewPagerAdapter(final FragmentManager fm) {
        super(fm);
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": constructor");
    }


    /**
     *
     */
    protected void init(final Redditor activity) {
        this.activity = activity;
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(final int position) {
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getItem(): position requested: " + position);
        return this.activity.getFragment(position);
    }


    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return LinkViewPagerAdapter.NUM_FRAGMENTS;
    }

}
