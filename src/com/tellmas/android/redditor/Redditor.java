package com.tellmas.android.redditor;

import java.net.URI;

import com.cd.reddit.RedditException;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
//import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
//import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


/**
 *
 */
public class Redditor extends ActionBarActivity { //FragmentActivity {

    private LinkViewPagerAdapter linkPagerAdapter;
    private ViewPagerWithCustomDuration linkViewPager;

    private LinksListFragment linksListFragment;
    private LinkDisplayFragment linkDisplayFragment;
    private Fragment currentlyDisplayedFragment;


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
       this.linkDisplayFragment = LinkDisplayFragment.newInstance(null);


       this.linkPagerAdapter = new LinkViewPagerAdapter(this.getSupportFragmentManager());
       this.linkPagerAdapter.init(this);
       this.linkViewPager = (ViewPagerWithCustomDuration) this.findViewById(R.id.redditor);
       this.linkViewPager.setScrollDurationFactor(GlobalDefines.SCROLL_DURATION_FACTOR);
       this.linkViewPager.setAdapter(this.linkPagerAdapter);
       this.linkViewPager.setOnPageChangeListener(new OnPageChangeListener() {
           @Override
           public void onPageSelected(final int position) {
               Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPageSelected()");

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
           default:
               fragment = this.currentlyDisplayedFragment;
       }
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getFragment(): returning Fragment type: " + fragment.getClass().getSimpleName());
       return fragment;
   }


   /**
    *
    */
   protected void displayNewLinkFragment(final URI newUrl) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayNewLinkFragment()");

       this.linkDisplayFragment.loadNewUrl(newUrl.toString());
       // === Switch to the we webview fragment ===
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

}
