package com.tellmas.android.redditor;

import java.net.URI;
import java.util.List;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cd.reddit.json.mapping.RedditLink;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 *
 */
public class LinksListAdapter extends BaseAdapter {

    protected final List<RedditLink> theList;
    protected final LayoutInflater inflater;
    protected final Activity activity;

    private final ImageLoader imageLoader;
    private final DisplayImageOptions imageloaderDisplayOptions;

    private final Drawable hourglass;


    /**
     * @param list TODO
     */
    public LinksListAdapter(final List<RedditLink> list, final Activity activity) {

        this.activity = activity;

        LayoutInflater li = null;
        try {
            li = activity.getLayoutInflater();
        } catch (final NullPointerException npe) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": LinkListAdapter(): 'activity' param was null", npe);
        }
        this.inflater = li;

        this.theList = list;
        if (list == null) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": LinkListAdapter(): 'list' param was null");
        }

        this.imageloaderDisplayOptions = new DisplayImageOptions.Builder()
        .bitmapConfig(Bitmap.Config.ARGB_8888)
        //.bitmapConfig(Bitmap.Config.RGB_565)
        .cacheInMemory(true)
        .cacheOnDisc(true)
        .build();
        this.imageLoader = ImageLoader.getInstance();

        this.hourglass = activity.getResources().getDrawable(R.drawable.hourglass);
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        int count;
        try {
            count = this.theList.size();
        } catch (final NullPointerException npe) {
            count = 0;
        }
        return count;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(final int position) {
        Object item;
        try {
            item = this.theList.get(position);
        } catch (final NullPointerException npe) {
            item = null;
        }
        return item;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(final int position) {

        long id;
        try {
            id = Long.getLong(this.theList.get(position).getId()).longValue();
        } catch (final NullPointerException npe) {
            id = position;
        }
        return id;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null || !(convertView instanceof ViewGroup)) {
            convertView = (ViewGroup) this.inflater.inflate(R.layout.link, parent, false);
        }


        final RedditLink theLink = (RedditLink) this.getItem(position);
        final boolean isSelf = theLink.isSelf();


        // --- Score ---
        final TextView scoreView = (TextView) convertView.findViewById(R.id.link_score);
        final int score = theLink.getUps() - theLink.getDowns();
        scoreView.setText(Integer.toString(score));

        // --- thumbnail ---
        final ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.link_thumbnail);
        // hide the thumbnail ImageView so if 'convertView' is being reused, the previous image won't be displayed
        thumbnailView.setVisibility(View.GONE);

        String thumbnailUri = theLink.getThumbnail();
        //Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): thumbnail uri: " + thumbnailUri);
        if (thumbnailUri.equals("self")) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): given thumbnail uri: " + thumbnailUri);
            thumbnailUri = "";
        } else if (thumbnailUri.equals("nsfw")) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): given thumbnail uri: " + thumbnailUri);
            thumbnailUri = "";
        } else if (thumbnailUri.equals("default")) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): given thumbnail uri: " + thumbnailUri);
            thumbnailUri = "";
        } else {
            try {
                // verify that thumbnail uri is a valid uri
                URI.create(thumbnailUri);
                // java.net.URI allows "self" w/o throwing the exception. Not sure why.
                // getThumbnail() returns "self" if there is no thumbnail node.
                // a temporary hack job:
                // TODO
            } catch (final IllegalArgumentException iae) {
                thumbnailUri = "";
            }
        }

        if (thumbnailUri.length() > 0) {
            try {
                thumbnailView.setImageDrawable(this.hourglass);
                thumbnailView.setVisibility(View.VISIBLE);
                this.imageLoader.displayImage(thumbnailUri, thumbnailView);
                //this.imageLoader.displayImage(thumbnailUri, thumbnailView, this.imageloaderDisplayOptions);
            } catch (final Exception e) {
                Log.e(GlobalDefines.LOG_TAG, "", e);
            }
        }

        // --- title ---
        final TextView idView = (TextView) convertView.findViewById(R.id.link_title);
        idView.setText(theLink.getTitle());

        // === submission time ago ===
        final TextView timeAgoView = (TextView) convertView.findViewById(R.id.link_time_ago);
        timeAgoView.setText(GlobalDefines.submissionTimeStringBuilder(theLink.getCreated_utc(), this.activity));

        // --- author ---
        final TextView authorView = (TextView) convertView.findViewById(R.id.link_author);
        authorView.setText(theLink.getAuthor());

        // --- subreddit ---
        final String subreddit = theLink.getSubreddit();
        final TextView subredditView = (TextView) convertView.findViewById(R.id.link_subreddit);
        subredditView.setText(GlobalDefines.SUBREDDIT_URI_PREFIX + subreddit);
        subredditView.setTag(subreddit);

        // === num comments ===
        final int numComments = theLink.getNum_comments();
        final TextView commentsView = (TextView) convertView.findViewById(R.id.link_comments_num);
        commentsView.setText(Integer.toString(numComments));
        if (numComments == 1) {
            final TextView commentsText = (TextView) convertView.findViewById(R.id.link_comments_text);
            commentsText.setText(R.string.comment);
        }

        // --- url ---
        final TextView urlView = (TextView) convertView.findViewById(R.id.link_url);
        String domain = null;
        if (isSelf) {
            domain = "self." + subreddit;
        } else {
            try {
                domain = URI.create(theLink.getUrl()).getHost();
            } catch (final IllegalArgumentException iae) {
                domain = "";
            }
        }
        urlView.setText(domain);


        return convertView;
    }

}
