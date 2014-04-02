package com.tellmas.android.redditor;

import android.util.Log;
import android.widget.AbsListView;

/**
 *
 */
public abstract class ScrollListener implements AbsListView.OnScrollListener {

   //private int currentFirstVisibleItem;
   //private int currentVisibleItemCount;

   // === ScrollListener Defaults ===
   private static final int DEFAULT_VISIBLE_THRESHOLD = 1;
   private static final int DEFAULT_STARTING_PAGE_INDEX = 0;


   // The minimum amount of items to have below your current scroll position before loading more.
   private final int visibleThreshold;
   // The current offset index of data you have loaded
   private int currentPage = 0;
   // The total number of items in the dataset after the last load
   private int previousTotalItemCount = 0;
   // True if we are still waiting for the last set of data to load.
   private boolean loading = true;
   // Sets the starting page index
   private final int startingPageIndex;

   /**
    *
    */
   public ScrollListener() {
       this.visibleThreshold = DEFAULT_VISIBLE_THRESHOLD;
       this.startingPageIndex = DEFAULT_STARTING_PAGE_INDEX;
       this.currentPage = this.startingPageIndex;
   }


   /**
    *
    * @param visibleThreshold
    */
   public ScrollListener(final int visibleThreshold) {
       this.visibleThreshold = DEFAULT_VISIBLE_THRESHOLD;
       this.startingPageIndex = DEFAULT_STARTING_PAGE_INDEX;
       this.currentPage = this.startingPageIndex;
   }


   /**
    *
    * @param visibleThreshold
    * @param startPage
    */
   public ScrollListener(final int visibleThreshold, final int startPage) {
       this.visibleThreshold = visibleThreshold;
       this.startingPageIndex = startPage;
       this.currentPage = startPage;
   }




   /**
    *
    */
   @Override
   public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onScroll()");
       //this.currentFirstVisibleItem = firstVisibleItem;
       //this.currentVisibleItemCount = visibleItemCount;

       // If the total item count is zero and the previous isn't, assume the
       // list is invalidated and should be reset back to initial state
       if (totalItemCount < this.previousTotalItemCount) {
           this.currentPage = this.startingPageIndex;
           this.previousTotalItemCount = totalItemCount;
           if (totalItemCount == 0) {
               this.loading = true;
           }
       }

       // If it’s still loading, we check to see if the dataset count has
       // changed, if so we conclude it has finished loading and update the current page
       // number and total item count.
       if (this.loading && (totalItemCount > this.previousTotalItemCount)) {
           this.loading = false;
           this.previousTotalItemCount = totalItemCount;
           this.currentPage++;
       }

       // If it isn’t currently loading, we check to see if we have breached
       // the visibleThreshold and need to reload more data.
       // If we do need to reload some more data, we execute onLoadMore to fetch the data.
       if (!this.loading && (totalItemCount - visibleItemCount)<=(firstVisibleItem + this.visibleThreshold)) {
           this.onTimeToLoadMoreData(this.currentPage + 1, totalItemCount);
           this.loading = true;
       }
   }

   /**
    *
    */
   @Override
   public void onScrollStateChanged(final AbsListView view, final int scrollState) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onScrollStateChanged()");
       // Not going to do anything.
   }


   /**
    *
    */
   public abstract void onTimeToLoadMoreData(int page, int totalItemsCount);
}
