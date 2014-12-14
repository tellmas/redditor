package com.tellmas.android.redditor;

import java.util.List;

import com.cd.reddit.Reddit;
import com.cd.reddit.RedditException;
import com.cd.reddit.http.util.RedditApiParameterConstants;
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
    private RedditLink linkData;

    private int displayCallCount = 0;


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

       if (this.subreddit != null && this.linkId != null) {
           this.loadNewComments(this.subreddit, this.linkId);
       }
   }


   /**
    *
    */
   public void loadNewComments(final String subreddit, final String linkId) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewComments()");
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewComments(): subreddit: " + subreddit + " - link id: " + linkId);

       new RequestCommentsTask().execute(subreddit, linkId);
       new RequestLinkTask().execute(linkId);
   }


   /*
    * TODO
    */
   private void displayComments() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": displayComments()");

       this.displayCallCount++;
       if (this.displayCallCount != 2) {
           return;
       }


       this.commentsListView = (ListView) CommentsFragment.this.getView().findViewById(R.id.comments_list);
       this.commentsListView.setVisibility(View.INVISIBLE);
       this.commentsListView.setAdapter(this.commentsListAdapter);

       //ViewGroup link_info_section = (ViewGroup) this.parentActivity.findViewById(R.id.link_info);
       ViewGroup linkInfoSection = (ViewGroup) this.parentActivity.getLayoutInflater().inflate(
               R.layout.comments_list_link_data,
               //this.commentsListView);
               null);
       //link_info_section.setVisibility(View.INVISIBLE);

       TextView scoreView = (TextView) linkInfoSection.findViewById(R.id.link_score);
       TextView titleView = (TextView) linkInfoSection.findViewById(R.id.link_title);
       TextView subredditView = (TextView) linkInfoSection.findViewById(R.id.link_subreddit);
       TextView timeView = (TextView) linkInfoSection.findViewById(R.id.link_time);
       TextView submitterView = (TextView) linkInfoSection.findViewById(R.id.link_submitter);

       scoreView.setText(Integer.toString(this.linkData.getUps() - this.linkData.getDowns()));
       titleView.setText(this.linkData.getTitle());
       subredditView.setText(GlobalDefines.SUBREDDIT_URI_PREFIX.concat(this.linkData.getSubreddit()));
       timeView.setText(GlobalDefines.submissionTimeStringBuilder(this.linkData.getCreated_utc(), this.parentActivity));
       submitterView.setText(this.linkData.getAuthor());

       this.commentsListView.addHeaderView(linkInfoSection);
       this.commentsLoadingProgressBar.setIndeterminate(false);
       this.commentsLoadingProgressBar.setProgress(this.commentsLoadingProgressBar.getMax());
       //linkInfoSection.setVisibility(View.VISIBLE);
       this.commentsListView.setVisibility(View.VISIBLE);
   }


   /**
    * TODO
    */
   private class RequestLinkTask extends AsyncTask<String, Integer, RedditLink> {

       ProgressBar progressBar = CommentsFragment.this.commentsLoadingProgressBar;

       /*
        * (non-Javadoc)
        * @see android.os.AsyncTask#onPreExecute()
        */
       @Override
       protected void onPreExecute() {
           this.progressBar.setProgress(0);
           this.progressBar.setIndeterminate(true);
       }


       /*
        * (non-Javadoc)
        * @see android.os.AsyncTask#doInBackground(Params[])
        */
       @Override
       protected RedditLink doInBackground(String... linkId) {
           Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": doInBackground()");
           Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": doInBackground(): link id: " + linkId[0]);

           RedditLink theLink = null;
           try {
               List <RedditLink> linkDataList = CommentsFragment.this.reddit.infoForId(
                       RedditApiParameterConstants.LINK_TYPE + linkId[0]);
               if (! linkDataList.isEmpty()) {
                   theLink = linkDataList.get(0);
               }
           } catch (final RedditException re) {
               Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": doInBackground(): Exception getting link data for: " + linkId[0], re);
           }
           return theLink;
       }


       /*
        * (non-Javadoc)
        * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
        */
       @Override
       protected void onPostExecute(RedditLink theLinkData) {
           Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPostExecute()");
           Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPostExecute(): Is the link data object null: " + theLinkData == null ? "yes" : "no");

           CommentsFragment.this.linkData = theLinkData;
           CommentsFragment.this.displayComments();
       }
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

            //publishProgress(25);
            RedditComments theComments = null;
            try {
                theComments = CommentsFragment.this.reddit.commentsFor(subredditAndLinkId[0], subredditAndLinkId[1]);
            } catch (final RedditException re) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": doInBackground(): Exception getting comments for: " + subredditAndLinkId[0] + " - link: " + subredditAndLinkId[1], re);
            }
            //publishProgress(50);
            return theComments;
        }


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onProgressUpdate(Params[])
         */
        protected void onProgressUpdate(Integer... progressPercentage) {
            this.progressBar.setProgress(progressPercentage[0]);
        }


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(RedditComments theComments) {

            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onPostExecute(): Is the comments object null: " + theComments == null ? "yes" : "no");

            //this.progressBar.setProgress(75);

            List<RedditComment> commentsList = null;
            try {
                commentsList = theComments.getComments();
            } catch (NullPointerException npe) {}
            CommentsFragment.this.commentsListAdapter = new CommentsListAdapter(
                    (Activity)CommentsFragment.this.parentActivity,
                    commentsList);
            //CommentsFragment.this.commentsListView = (ListView) CommentsFragment.this.getView().findViewById(R.id.comments_list);
            //CommentsFragment.this.commentsListView.setVisibility(View.INVISIBLE);
            //CommentsFragment.this.commentsListView.setAdapter(commentsListAdapter);
            CommentsFragment.this.displayComments();

            //this.progressBar.setProgress(100);
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
