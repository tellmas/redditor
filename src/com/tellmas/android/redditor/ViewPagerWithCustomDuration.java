package com.tellmas.android.redditor;

import java.lang.reflect.Field;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 *
 */
public class ViewPagerWithCustomDuration extends ViewPager {

   private ScrollerWithCustomDuration scroller;


   /**
    *
    * @param context
    */
   public ViewPagerWithCustomDuration(final Context context) {
       super(context);
       this.postInitViewPager();
   }


   /**
    *
    * @param context
    * @param attrs
    */
   public ViewPagerWithCustomDuration(final Context context, final AttributeSet attrs) {
       super(context, attrs);
       this.postInitViewPager();
   }


   /**
    * Override the Scroller instance with our own class so we can change the duration
    */
   private void postInitViewPager() {
       try {
           final Class<?> viewpager = ViewPager.class;
           final Field scroller = viewpager.getDeclaredField("mScroller");
           scroller.setAccessible(true);
           final Field interpolator = viewpager.getDeclaredField("sInterpolator");
           interpolator.setAccessible(true);

           this.scroller = new ScrollerWithCustomDuration(
                   this.getContext(),
                   (Interpolator) interpolator.get(null));
           scroller.set(this, this.scroller);
       } catch (final Exception e) {
           Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + "postInitViewPager(): error: ", e);
       }
   }


   /**
    * Set the factor by which the duration will change
    * @param scrollFactor
    */
   public void setScrollDurationFactor(final double scrollFactor) {
       this.scroller.setScrollDurationFactor(scrollFactor);
   }



   /**
    *
    */
   public class ScrollerWithCustomDuration extends Scroller {

       private double scrollFactor = 1;


       /**
        *
        * @param context
        */
       public ScrollerWithCustomDuration(final Context context) {
           super(context);
       }

       /**
        *
        * @param context
        * @param interpolator
        */
       public ScrollerWithCustomDuration(final Context context, final Interpolator interpolator) {
           super(context, interpolator);
       }


       /**
        *
        * @param context
        * @param interpolator
        * @param flywheel
        */
       public ScrollerWithCustomDuration(final Context context, final Interpolator interpolator, final boolean flywheel) {
           super(context, interpolator, flywheel);
       }


       /**
        * Set the factor by which the duration will change
        */
       public void setScrollDurationFactor(final double scrollFactor) {
           this.scrollFactor = scrollFactor;
       }


       /**
        *
        */
       @Override
       public void startScroll(final int startX, final int startY, final int dx, final int dy, final int duration) {
           super.startScroll(startX, startY, dx, dy, (int) (duration * this.scrollFactor));
       }

   } // end class ScrollerWithCustomDuration


} // end class ViewPagerWithCustomDuration
