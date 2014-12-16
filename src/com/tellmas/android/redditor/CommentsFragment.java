package com.tellmas.android.redditor;

import java.util.List;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.json.mapping.RedditComment;
import com.cd.reddit.json.mapping.RedditLink;
import com.cd.reddit.json.util.RedditComments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *
 */
public class CommentsFragment extends Fragment {

    private String subreddit = null;
    private String linkId = null;

    private Redditor parentActivity;
    private ProgressBar commentsLoadingProgressBar;

    private Reddit reddit;

    private ListView commentsListView;

    private CommentsListAdapter commentsListAdapter;
    private RedditLink theLinkData;


    /**
     *
     */
    public static CommentsFragment newInstance(final String subreddit, final String linkId) {
        final CommentsFragment thisFragment = new CommentsFragment();

        final Bundle args = new Bundle();
        args.putString(GlobalDefines.BUNDLE_KEY_FOR_SUBREDDIT_NAME, subreddit);
        args.putString(GlobalDefines.BUNDLE_KEY_FOR_LINK_ID, linkId);
        thisFragment.setArguments(args);

        return thisFragment;
    }


   /**
    *
    */
   @Override
   public void onAttach(final Activity activity) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onAttach()");
       super.onAttach(activity);

       this.parentActivity = (Redditor) activity;
   }


    /**
     *
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate()");
        super.onCreate(savedInstanceState);

        // --- The raw4j Reddit object ---
        this.reddit = this.parentActivity.getRedditObject();

        try {
            this.subreddit = savedInstanceState.getString(GlobalDefines.BUNDLE_KEY_FOR_SUBREDDIT_NAME);
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): subreddit: " + this.subreddit);
            this.linkId = savedInstanceState.getString(GlobalDefines.BUNDLE_KEY_FOR_LINK_ID);
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): link id: " + this.linkId);
        } catch (final NullPointerException npe) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): missing parameters in the Bundle");
        } catch (final Exception e) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreate(): some other exception...", e);
        }
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

       return inflater.inflate(R.layout.comments_fragment, container, false);
   }


   /**
    *
    */
   @Override
   public void onViewCreated(final View view, final Bundle savedInstanceState) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onViewCreated()");

       // --- progress bar for comments loading ---
       this.commentsLoadingProgressBar = (ProgressBar) this.getView().findViewById(R.id.comments_loading_progress);
   }


   /**
    *
    */
   public void loadNewComments(final RedditLink linkData) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewComments()");

       this.theLinkData = linkData;

       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewComments(): subreddit: " + linkData.getSubreddit());
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewComments(): link id: " + linkData.getId());

       this.commentsLoadingProgressBar.setIndeterminate(true);
       new RequestCommentsTask().execute(linkData.getSubreddit(), linkData.getId());
   }


   /*
    * TODO
    */
   private void displayComments() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayComments()");

       this.commentsListView = (ListView) CommentsFragment.this.getView().findViewById(R.id.comments_list);
       this.commentsListView.setVisibility(View.INVISIBLE);
       this.commentsListView.setAdapter(this.commentsListAdapter);

       ViewGroup linkInfoSection = (ViewGroup) this.parentActivity.getLayoutInflater().inflate(
               R.layout.comments_list_link_data,
               null);

       TextView scoreView = (TextView) linkInfoSection.findViewById(R.id.link_score);
       TextView titleView = (TextView) linkInfoSection.findViewById(R.id.link_title);
       TextView subredditView = (TextView) linkInfoSection.findViewById(R.id.link_subreddit);
       TextView timeView = (TextView) linkInfoSection.findViewById(R.id.link_time);
       TextView submitterView = (TextView) linkInfoSection.findViewById(R.id.link_submitter);

       scoreView.setText(Integer.toString(this.theLinkData.getUps() - this.theLinkData.getDowns()));
       titleView.setText(this.theLinkData.getTitle());
       subredditView.setText(GlobalDefines.SUBREDDIT_URI_PREFIX.concat(this.theLinkData.getSubreddit()));
       timeView.setText(GlobalDefines.submissionTimeStringBuilder(this.theLinkData.getCreated_utc(), this.parentActivity));
       submitterView.setText(this.theLinkData.getAuthor());

       this.commentsListView.addHeaderView(linkInfoSection);
       this.commentsLoadingProgressBar.setIndeterminate(false);
       this.commentsLoadingProgressBar.setProgress(this.commentsLoadingProgressBar.getMax());
       this.commentsLoadingProgressBar.setProgress(100);
       this.commentsListView.setVisibility(View.VISIBLE);
   }


    /**
     * TODO
     */
    private class RequestCommentsTask extends AsyncTask<String, Integer, RedditComments> {

        ProgressBar progressBar = CommentsFragment.this.commentsLoadingProgressBar;

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            this.progressBar.setIndeterminate(true);
            this.progressBar.setProgress(0);
        }


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected RedditComments doInBackground(String... subredditAndLinkId) {

            RedditComments theComments = null;
            try {
                theComments = CommentsFragment.this.reddit.commentsFor(subredditAndLinkId[0], subredditAndLinkId[1]);
            } catch (final RedditException re) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": doInBackground(): Exception getting comments for: " + subredditAndLinkId[0] + " - link: " + subredditAndLinkId[1], re);
            }
            return theComments;
        }


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(RedditComments theComments) {

            List<RedditComment> commentsList = null;
            try {
                commentsList = theComments.getComments();
            } catch (NullPointerException npe) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPostExecute(): null RedditComments object");
            }
            CommentsFragment.this.commentsListAdapter = new CommentsListAdapter(
                    (Activity)CommentsFragment.this.parentActivity,
                    commentsList);
            CommentsFragment.this.displayComments();
        }
    }


    /*
     * TODO
     */
    static class CommentViewHolder {

        public TextView commentTextView;
        public TextView commentAuthorView;
        public TextView commentScoreView;
        public TextView commentTimeView;
    }
}
