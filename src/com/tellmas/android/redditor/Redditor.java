package com.tellmas.android.redditor;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.os.Bundle;
import android.util.Log;


/**
 *
 */
public class Redditor extends Activity {

    private LinkViewPagerAdapter linkPagerAdapter;
    private ViewPagerWithCustomDuration linkViewPager;

    private LinksListFragment linksListFragment;
    private LinkDisplayFragment linkDisplayFragment;
    private CommentsFragment commentsFragment;
    private Fragment currentlyDisplayedFragment;

    private Reddit reddit;

    private final Redditor self = this;


   /**
    *
    */
   @Override
   protected void onCreate(final Bundle savedInstanceState) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate()");
       super.onCreate(savedInstanceState);

       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): setting the content view");
       try {
           this.setContentView(R.layout.activity_redditor);
       } catch (final Exception e) {
           Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): error setting the content view", e);
           System.exit(GlobalDefines.EXIT_STATUS_ERROR);
       }

       // === LinksListFragment ===
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): instantiating the LinksListFragment");
       this.linksListFragment = LinksListFragment.newInstance();
       this.currentlyDisplayedFragment = this.linksListFragment;

       // === LinkDisplayFragment ===
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): instantiating a LinkDisplayFragment");
       this.linkDisplayFragment = LinkDisplayFragment.newInstance(null, null, null);

       // === CommentsFragment ===
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): instantiating a CommentsFragment");
       this.commentsFragment = CommentsFragment.newInstance(null, null);

       this.linkPagerAdapter = new LinkViewPagerAdapter(this.getFragmentManager());
       this.linkPagerAdapter.init(this);
       this.linkViewPager = (ViewPagerWithCustomDuration) this.findViewById(R.id.redditor);
       this.linkViewPager.setScrollDurationFactor(GlobalDefines.SCROLL_DURATION_FACTOR);
       this.linkViewPager.setAdapter(this.linkPagerAdapter);
       this.linkViewPager.setOnPageChangeListener(new OnPageChangeListener() {
           @Override
           public void onPageSelected(final int position) {
               Log.d(GlobalDefines.LOG_TAG, "OnPageChangeListener: onPageSelected()");

               switch (position) {
                   case 0:
                       Redditor.this.self.currentlyDisplayedFragment = Redditor.this.self.linksListFragment;
                       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPageSelected(): setting currently displayed fragment to linksListFragment");
                       break;
                   case 1:
                       Redditor.this.self.currentlyDisplayedFragment = Redditor.this.self.linkDisplayFragment;
                       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPageSelected(): setting currently displayed fragment to linkDisplayFragment");
                       break;
               }
           }

           @Override
           public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
           }

           @Override
           public void onPageScrollStateChanged(final int state) {
           }
       });

       // --- The raw4j master object ---
       this.reddit = new Reddit(GlobalDefines.USER_AGENT);
   }


   /**
    *
    */
   public Fragment getCurrentlyDisplayedFragment() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getCurrentlyDisplayedFragment()");

       return this.currentlyDisplayedFragment;
   }


   /**
    *
    */
   public Fragment getFragment(final int position) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getFragment()");
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getFragment(): requested position: " + position);

       Fragment fragment = null;
       switch (position) {
           case 0:
               fragment = this.linksListFragment;
               break;
           case 1:
               fragment = this.linkDisplayFragment;
               break;
           case 2:
               fragment = this.commentsFragment;
               break;
           default:
               fragment = this.currentlyDisplayedFragment;
       }
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getFragment(): returning Fragment type: " + fragment.getClass().getSimpleName());
       return fragment;
   }


   /**
    *
    */
   protected void displayCommentsFragment(final RedditLink linkData) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayCommentsFragment()");

       this.commentsFragment.loadNewComments(linkData);
       // === Switch to the comments fragment ===
       this.linkViewPager.setCurrentItem(2, true);
       this.currentlyDisplayedFragment = this.commentsFragment;
   }


   /**
    *
    */
   protected void displayNewLinkFragment(final RedditLink theLinkData) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayNewLinkFragment()");

       this.linkDisplayFragment.loadNewLink(theLinkData);
       // === Switch to the webview fragment ===
       this.linkViewPager.setCurrentItem(1, true);
       this.currentlyDisplayedFragment = this.linkDisplayFragment;
   }


   /**
    *
    */
   protected void displayListingFragment() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayListingFragment()");

       if (this.currentlyDisplayedFragment != this.linksListFragment) {
           this.linkViewPager.setCurrentItem(0, true);
           this.currentlyDisplayedFragment = this.linksListFragment;
       }
   }


   /**
    *
    */
   @Override
   public void onBackPressed() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onBackPressed()");

       if (this.currentlyDisplayedFragment == this.linkDisplayFragment) {
           this.linkViewPager.setCurrentItem(0, true);
           this.currentlyDisplayedFragment = this.linksListFragment;
       } else if (this.currentlyDisplayedFragment == this.commentsFragment) {
           this.linkViewPager.setCurrentItem(1, true);
           this.currentlyDisplayedFragment = this.linkDisplayFragment;
       } else {
           super.onBackPressed();
       }
   }


   /**
    *
    */
   protected void handleRedditException(final RedditException re) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": handleRedditException()");

   }


   /**
    *
    */
   public Reddit getRedditObject() {
       return this.reddit;
   }

}
