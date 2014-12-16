package com.tellmas.android.redditor;

import com.cd.reddit.json.mapping.RedditComment;
import com.cd.reddit.json.util.RedditJsonConstants;
import com.tellmas.android.redditor.CommentsFragment.CommentViewHolder;

import android.app.Activity;
import android.database.DataSetObserver;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class CommentsListAdapter implements ListAdapter {


    public LayoutInflater inflater;
    private final Activity parentActivity;
    private final List<RedditComment> commentsList;


    /**
     *
     */
    public CommentsListAdapter(final Activity parentActivity, final List<RedditComment> theListOfComments) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": CommentsListAdapter() constructor");

        if (theListOfComments == null) {
            this.commentsList = new ArrayList<RedditComment>(0);
        } else {
            this.commentsList = theListOfComments;
        }
        this.parentActivity = parentActivity;
        this.inflater = parentActivity.getLayoutInflater();
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return this.commentsList.size();
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int parentCommentPosition) {
        return this.commentsList.get(parentCommentPosition);
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int parentCommentPosition) {
        return parentCommentPosition;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int parentCommentPosition) {
        return 0;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View commentContainer, final ViewGroup parentView) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView()");

        final CommentViewHolder commentViewHolder;

        if (commentContainer == null) {
            commentContainer = this.inflater.inflate(R.layout.comments_list_comment_container, parentView, false);

            final View commentView = this.inflater.inflate(R.layout.comments_list_comment_view, (ViewGroup)commentContainer, false);
            commentViewHolder = new CommentViewHolder();
            commentViewHolder.commentTextView = (TextView) commentView.findViewById(R.id.comment_view_text);
            commentViewHolder.commentAuthorView = (TextView) commentView.findViewById(R.id.comment_view_author);
            commentViewHolder.commentScoreView = (TextView) commentView.findViewById(R.id.comment_view_score);
            commentViewHolder.commentTimeView = (TextView) commentView.findViewById(R.id.comment_view_time);

            // Add the comment view to the comment container.
            ((ViewGroup) commentContainer).addView(commentView);

            // Store the view holder.
            commentContainer.setTag(R.id.comment_view, commentViewHolder);
        } else {
            commentViewHolder = (CommentViewHolder) commentContainer.getTag(R.id.comment_view);
            // Remove reply comment views (obviously skipping the parent comment (i.e. the direct reply to the posting)).
            ((ViewGroup) commentContainer).removeViews(
                    1,
                    ((Integer) commentContainer.getTag(R.id.comment_view_container)).intValue() - 1);
        }
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): 'commentContainer' class: "+ commentContainer.getClass().toString());
        // Set number of so far added Views.
        commentContainer.setTag(R.id.comment_view_container, Integer.valueOf(1));


        final RedditComment commentData = (RedditComment) this.getItem(position);
        // Set the actual comment text.
        commentViewHolder.commentTextView.setText(Html.fromHtml(commentData.getBody()));
        // Author of comment
        commentViewHolder.commentAuthorView.setText(commentData.getAuthor());
        // Score of comment
        commentViewHolder.commentScoreView.setText(
                commentViewHolder.commentScoreView.getText().toString().replace(
                        GlobalDefines.STRING_REPLACEMENT,
                        Integer.toString(commentData.getUps() - commentData.getDowns())));
        // Time submitted of comment
        commentViewHolder.commentTimeView.setText(
                GlobalDefines.submissionTimeStringBuilder(commentData.getCreated_utc(), this.parentActivity));

        ObjectNode replies;
        try {

            // TODO need to check for a node that indicates more replies and display that.

            replies = (ObjectNode) commentData.getReplies();
        // if the returned object is not an ObjectNode...
        } catch (ClassCastException cce) {
            // ...it's probably a TextNode and thus there are no replies.
            // So, skip it.
            return commentContainer;
        }
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): getReplies() returns object of type: " + replies.getClass().toString());

        //Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): replies: " + replies.toString());

        JsonNode jn = replies.get(RedditJsonConstants.DATA);
        jn = jn.get(RedditJsonConstants.CHILDREN);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jn.toString());
        } catch (JSONException je) {}
        int arrayLen = jsonArray.length();
        for (int i=0; i < arrayLen; i++) {
            JSONObject reply = null;
            try {
                reply = new JSONObject(jsonArray.get(i).toString());
            } catch (JSONException je) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): exception converting reply data to a JSONObject", je);
            }
            this.addReplies(reply, (ViewGroup)commentContainer, 1);
        }

        return commentContainer;
    }


    /*
     * TODO
     */
    private void addReplies(JSONObject reply, ViewGroup containerView, int level) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies()");

        final int indentWidth = 30;

        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): 'reply' is an object of type: " + reply.getClass().toString());
        //Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): 'reply': " + reply.toString());

        Spanned commentText;
        String commentScore;
        String commentTime;
        String commentAuthor;
        String commentId = "";
        JSONObject commentData;
        try {
            String redditType = (String) reply.get(RedditJsonConstants.KIND);
            Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Type of reddit object: " + redditType);
            if (redditType.equals(RedditJsonConstants.TYPE_COMMENT)) {
                commentData = new JSONObject(reply.get(RedditJsonConstants.DATA).toString());
                commentId = commentData.get(RedditJsonConstants.NAME).toString();

                commentText = Html.fromHtml(commentData.get("body").toString()); // TODO add body to the RedditJsonConstants
                commentAuthor = commentData.get("author").toString();
                commentScore = commentData.get("score").toString();
                commentTime = commentData.get("created_utc").toString();
            } else if (redditType.equals(GlobalDefines.REDDIT_TYPE_MORE)) {
                Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Reached a 'more' section of the comments.");
                commentData = new JSONObject(); // TODO placeholder
                return; // TODO placeholder
            } else {
                throw new Exception("Not of type \"comment\"");
            }
        } catch (Exception e) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Error getting the reply comment's data", e);
            // Skip this comment.
            return;
        }

        // --- Create and attach the comment View. ---
        final View commentView = this.inflater.inflate(R.layout.comments_list_comment_view, (ViewGroup)containerView, false);
        commentView.setPadding(
                indentWidth * level
                ,commentView.getPaddingTop()
                ,commentView.getPaddingRight()
                ,commentView.getPaddingBottom());
        // TODO this repeats the same thing as in getView(). create a helper method to do these
        final TextView commentTextView = (TextView) commentView.findViewById(R.id.comment_view_text);
        final TextView commentAuthorView = (TextView) commentView.findViewById(R.id.comment_view_author);
        final TextView commentScoreView = (TextView) commentView.findViewById(R.id.comment_view_score);
        final TextView commentTimeView = (TextView) commentView.findViewById(R.id.comment_view_time);

        // Set the text.
        commentTextView.setText(commentText);
        commentAuthorView.setText(commentAuthor);
        try {
            commentTimeView.setText(GlobalDefines.submissionTimeStringBuilder(commentTime, this.parentActivity));
        } catch (NumberFormatException nfe) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Submission time for comment " + commentId + " is not a valid number: " + commentTime);
        }
        commentScoreView.setText(
                commentScoreView.getText().toString().replace(
                        GlobalDefines.STRING_REPLACEMENT,
                        commentScore));

        containerView.addView(commentView);
        // Update the number of comment views added to the container.
        containerView.setTag(
                R.id.comment_view_container,
                ((Integer) containerView.getTag(R.id.comment_view_container)).intValue() + 1
        );

        // === Replies to this reply. ===
        // if no replies...
        if (!commentData.has("replies")) {
            Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): No replies to this comment: " + commentId);
            // ...skip getting replies (obviously).
            return;
        }
        Object repliesObject;
        JSONObject repliesJSONObject;
        JSONArray theReplies = new JSONArray();
        try {
            repliesObject = commentData.get("replies");
            // if the replies field is empty...
            if (repliesObject.getClass().equals(String.class)) {
                Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): No replies to this comment: " + commentId);
                // ...skip getting replies (obviously).
                return;
            }
            repliesJSONObject = (JSONObject) repliesObject;
            repliesJSONObject = repliesJSONObject.getJSONObject("data");
            theReplies = repliesJSONObject.getJSONArray("children");
            Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): The replies JSONObject: " + theReplies.toString());
        } catch (Exception e) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Error getting the replies to this reply.", e);
            return;
        }
        int thRepliesArrayLen = theReplies.length();
        for (int i=0; i < thRepliesArrayLen; i++) {
            try {
                JSONObject aReply = theReplies.getJSONObject(i);
                this.addReplies(aReply, containerView, level + 1);
            } catch (JSONException je) {
                Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": addReplies(): Error getting the reply at index " + i + " to the reply: " + commentId, je);
            }
        }
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.commentsList.isEmpty();
    }


    /*
     * (non-Javadoc)
     * @see android.widget.ListAdapter#areAllItemsEnabled()
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.ListAdapter#isEnabled(int)
     */
    @Override
    public boolean isEnabled(int parentCommentPosition) {
        return true;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        return;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        return;
    }
}
