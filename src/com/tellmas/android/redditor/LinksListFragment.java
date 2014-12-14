package com.tellmas.android.redditor;

import java.net.URI;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditLink;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


/**
 *
 *  @extends Fragment
 */
public class LinksListFragment extends Fragment implements ActionBar.OnNavigationListener {


    private Redditor parentActivity;

    private Reddit reddit;

    private boolean isRetrievingLinks = false;
    private boolean isAdditionalPageOfListing = false;

    protected ListView listView;
    protected List<RedditLink> links;
    private LinksListAdapter listAdapter;

    private String theBeforeLink = null;
    private String theAfterLink = null;
    private int runningCountOfLinksRetrieved = 0;

    //private String currentSubreddit = GlobalDefines.DEFAULT_LISTING;
    protected String currentSort = GlobalDefines.DEFAULT_SORT;

    protected int currentSubredditIndex = 0;
    protected String[] subreddits;
    protected String[] subredditDisplayNames;

    protected int currentSortIndex = 0;
    protected String[] sortByDisplayStrings;


    // --- the hourglass ---
    private ProgressBar hourglass;


    // --- when finished loading Reddit links ---
    private final Handler linksHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {

            final List<RedditLink> newLinks;
            final RedditResponse response;
            try {
                response = (RedditResponse) msg.obj;
                newLinks = (List<RedditLink>) response.getLinksList();
            } catch (final ClassCastException cce) {
                Log.e(GlobalDefines.LOG_TAG, "Handler: handleMessage(): linksHandler: data received not correct type.");
                return;
            }

            LinksListFragment.this.theBeforeLink = response.getBefore();
            Log.v(GlobalDefines.LOG_TAG, "Handler: handleMessage(): the new before: " + LinksListFragment.this.theBeforeLink);
            LinksListFragment.this.theAfterLink = response.getAfter();
            Log.v(GlobalDefines.LOG_TAG, "Handler: handleMessage(): the new after: " + LinksListFragment.this.theAfterLink);

            LinksListFragment.this.isRetrievingLinks = false;
            //LinksListFragment.this.updateSpinner();
            LinksListFragment.this.toggleHourGlass();

            LinksListFragment.this.runningCountOfLinksRetrieved += newLinks.size();

            if (LinksListFragment.this.isAdditionalPageOfListing) {
                LinksListFragment.this.isAdditionalPageOfListing = false;
                LinksListFragment.this.links.addAll(newLinks);
                Log.v(GlobalDefines.LOG_TAG, "Handler: handleMessage(): num of new links received: " + Integer.valueOf(newLinks.size()).toString());
            } else {
                LinksListFragment.this.links = newLinks;
                LinksListFragment.this.listAdapter = new LinksListAdapter(LinksListFragment.this.links, LinksListFragment.this.parentActivity);
                LinksListFragment.this.listView.setAdapter(LinksListFragment.this.listAdapter);
                Log.v(GlobalDefines.LOG_TAG, "Handler: handleMessage(): num of links: " + Integer.valueOf(LinksListFragment.this.links.size()).toString());
            }
            LinksListFragment.this.listAdapter.notifyDataSetChanged();
        }
    };


    /**
     *
     */
    public static LinksListFragment newInstance() {
        Log.d(GlobalDefines.LOG_TAG, "LinksListFragment: newInstance()");

        return new LinksListFragment();
    }


    /**
     *
     */
    @Override
    public void onAttach(final Activity activity) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onAttach()");
        super.onAttach(activity);

        this.parentActivity = (Redditor) activity;

        //
        this.sortByDisplayStrings = this.getResources().getStringArray(R.array.sort_by_strings);
    }


    /**
     *
     */
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState
    ) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        this.setHasOptionsMenu(true);

        return inflater.inflate(R.layout.listing_fragment, container, false);
    }


    /**
     *
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onActivityCreated()");

        // === the list of links ===
        this.listView = (ListView) this.getView().findViewById(R.id.list);
        this.listView.setOnScrollListener(new ScrollListener() {
            @Override
            public void onTimeToLoadMoreData(final int page, final int totalItemsCount) {
                LinksListFragment.this.expandListing();
            }
        });
        //this.listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //public void onItemSelected(final AdapterView<?> parentView, final View childView, final int position, final long id) {
            public void onItemClick(final AdapterView<?> parentView, final View childView, final int position, final long id) {
                //Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onItemSelected()");
                Log.d(GlobalDefines.LOG_TAG, "AdapterView.OnItemClickListener: onItemClick()");
                final RedditLink link = LinksListFragment.this.links.get(position);
                final String linkUrl = link.getUrl();
                URI theLinkUri = null;
                try {
                    theLinkUri = URI.create(linkUrl);
                } catch (final IllegalArgumentException iae) {
                    Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onItemClick(): " + linkUrl + " is not a proper url");
                }
                LinksListFragment.this.parentActivity.displayNewLinkFragment(
                        link.getSubreddit(),
                        link.getId(),
                        theLinkUri);

            }
        });

        // === the hourglass ===
        this.hourglass = (ProgressBar) this.getView().findViewById(R.id.hourglass);
        this.hourglass.setProgress(0);

        // === UniversalImageLoader setup ===
        final DisplayImageOptions imageloaderDisplayOptions = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            //.bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .build();
        final ImageLoaderConfiguration imageloaderConfig = new ImageLoaderConfiguration.Builder(this.parentActivity.getApplicationContext())
            .defaultDisplayImageOptions(imageloaderDisplayOptions)
            .build();
        ImageLoader.getInstance().init(imageloaderConfig);


        // === Reddit api library setup ===
        this.reddit = this.parentActivity.getRedditObject();


        // === Subreddit Picker (Spinner) ===
        this.subreddits = this.getResources().getStringArray(R.array.subreddit_picker_subreddits);
        this.subredditDisplayNames = this.getResources().getStringArray(R.array.subreddit_picker_subreddit_display_names);
        final SubredditPickerAdapter<String> subredditPickerAdapter =
                new SubredditPickerAdapter<String>(this.parentActivity, R.layout.layout_subreddit_picker_header, this.subredditDisplayNames, GlobalDefines.DEFAULT_SORT);
        subredditPickerAdapter.setDropDownViewResource(R.layout.layout_subreddit_picker_item);
        final ActionBar actionBar = this.parentActivity.getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onActivityCreated(): setting subreddit picker callbacks");
        actionBar.setListNavigationCallbacks(subredditPickerAdapter, this);
    }


    /**
     *
     */
    private void getAndDisplayListing(final String subreddit, final String sortBy, final String before, final String after) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getAndDisplayListing()");

        if (!this.isAdditionalPageOfListing) {
            try {
                this.links.clear();
            // if this is the first listing retrieved...
            } catch (final NullPointerException npe) {
                // ...just continue.
            }
        }

        this.isRetrievingLinks = true;
        this.updateSpinner();
        this.toggleHourGlass();

        final GetListingRunnable getListing = new GetListingRunnable(
                this.reddit,
                this.linksHandler,
                subreddit,
                sortBy,
                before,
                after,
                this.runningCountOfLinksRetrieved
        );
        final Thread getAListingThread = new Thread(getListing);
        getAListingThread.start();
    }


    /**
     *
     * @implements Runnable
     */
    private class GetListingRunnable implements Runnable {
        private final Reddit reddit;
        private final Handler handler;
        private final String subreddit;
        private final String sortBy;
        private final String before;
        private final String after;
        private final int count;

        GetListingRunnable(
                final Reddit reddit,
                final Handler handler,
                final String subreddit,
                final String sortBy,
                final String before,
                final String after,
                final int count) {
            this.reddit = reddit;
            this.handler = handler;
            this.subreddit = subreddit;
            this.sortBy = sortBy;
            this.before = before;
            this.after = after;
            this.count = count;
        }

        @Override
        public void run() {
            final Message msg = this.handler.obtainMessage();
            RedditResponse response;

            List<RedditLink> links = null;
            final StringBuilder valueOfBefore = new StringBuilder();
            final StringBuilder valueOfAfter = new StringBuilder();
            try {
                links = this.reddit.listingFor(this.subreddit, this.sortBy, this.before, this.after, -1, this.count, null, valueOfBefore, valueOfAfter);
                Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": run(): Sending request for:" +
                        " subreddit: \"" + this.subreddit + "\"" +
                        ", sort by: \"" + this.sortBy + "\"" +
                        ", before: \"" + this.before + "\"" +
                        ", after: \"" + this.after + "\"" +
                        ", count: \"" + Integer.valueOf(this.count).toString() + "\"");
            } catch (final RedditException re) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": run(): RedditException getting the link listing", re);
                LinksListFragment.this.parentActivity.handleRedditException(re);
                return;
            } catch (final Exception e) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": run(): Exception getting the link listing: " + e.getClass().getSimpleName(), e);
            }
            String valueOfBeforeAsString = valueOfBefore.toString();
            if (valueOfBeforeAsString.length() == 0) {
                valueOfBeforeAsString = null;
            }
            String valueOfAfterAsString = valueOfAfter.toString();
            if (valueOfAfterAsString.length() == 0) {
                valueOfAfterAsString = null;
            }
            response = new RedditResponse(links, valueOfBeforeAsString, valueOfAfterAsString);
            msg.obj = response;
            this.handler.sendMessage(msg);
        }
    }


    /**
     *
     */
    private void expandListing() {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": expandListing()");

        this.isAdditionalPageOfListing = true;
        this.getAndDisplayListing(this.subreddits[this.currentSubredditIndex], this.currentSort, this.theBeforeLink, this.theAfterLink);
    }


    /**
     *
     */
    private void updateSpinner() {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": updateSpinner()");

        final TextView subredditName = (TextView) this.parentActivity.findViewById(R.id.subreddit_picker_subreddit);
        try {
            subredditName.setText(this.subredditDisplayNames[this.currentSubredditIndex]);
        } catch (final Exception e) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": updateSpinner(): error updating the subreddit name in the Spinner", e);
        }

        final TextView sortByText = (TextView) this.parentActivity.findViewById(R.id.subreddit_picker_sort_by);
        try {
            sortByText.setText(this.currentSort);
        } catch (final Exception e) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": updateSpinner(): error updating the sort by text in the Spinner", e);
        }
    }


    /**
     * TODO
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflator) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateOptionsMenu()");

        inflator.inflate(R.menu.sort_menu, menu);
    }


    /**
     *
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onOptionsItemSelected()");

        final String sortBy = item.getTitleCondensed().toString();
        this.currentSort = sortBy;
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onOptionsItemSelected(): sort by: " + sortBy);
        final int itemId = item.getItemId();
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onOptionsItemSelected(): selected sort by item id: " + itemId);

        // --- Clear the listing parameters since we're getting a new subreddit. ---
        this.theBeforeLink = null;
        this.theAfterLink = null;
        this.runningCountOfLinksRetrieved = 0;

        this.getAndDisplayListing(this.subreddits[this.currentSubredditIndex], this.currentSort, this.theBeforeLink, this.theAfterLink);

        return true;
    }


    /**
     * Called when an item is selected in the Subreddit Picker
     */
    @Override
    public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onNavigationItemSelected()");

        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onNavigationItemSelected(): " +
                "position: " + Integer.valueOf(itemPosition).toString() +
                " item id: " + Long.valueOf(itemId).toString());

        // Update the subreddit in the spinner's header.
        this.currentSubredditIndex = itemPosition;

        // --- Clear the listing parameters since we're getting a new subreddit. ---
        this.theBeforeLink = null;
        this.theAfterLink = null;
        this.runningCountOfLinksRetrieved = 0;

        // --- Set the sort back to the default. ---
        this.currentSortIndex = 0;
        this.currentSort = this.sortByDisplayStrings[this.currentSortIndex];

        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onNavigationItemSelected(): getting listing for subreddit: " + this.subreddits[this.currentSubredditIndex]);
        // Get the new listing (and display it).
        this.getAndDisplayListing(this.subreddits[this.currentSubredditIndex], this.currentSort, this.theBeforeLink, this.theAfterLink);

        this.parentActivity.displayListingFragment();

        return true;
    }


   /**
    *
    */
   public void onClickForSubreddit(final View view) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onClickForSubreddit()");
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onClickForSubreddit(): new subreddit: " + this.subredditDisplayNames[this.currentSubredditIndex]);

       // get the new listing (and display it)
       this.getAndDisplayListing(this.subreddits[this.currentSubredditIndex], this.currentSort, this.theBeforeLink, this.theAfterLink);

       this.updateSpinner();
   }


    /*
     *
     */
    private boolean toggleHourGlass() {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": toggleHourGlass()");

        int hgVisibility;

        try {
            hgVisibility = this.hourglass.getVisibility();
        } catch (final Exception e) {
            Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": toggleHourGlass(): error getting visibility", e);
            return false;
        }
        try {
            switch (hgVisibility) {
                case View.VISIBLE:
                    this.hourglass.setVisibility(View.GONE);
                    break;
                case View.GONE:
                    this.hourglass.setVisibility(View.VISIBLE);
                    break;
            }
            this.hourglass.setProgress(0);
        } catch (final Exception e) {
            Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": toggleHourGlass(): error setting visibility", e);
            return false;
        }


        return true;
    }


}
