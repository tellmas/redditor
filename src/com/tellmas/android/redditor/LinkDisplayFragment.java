package com.tellmas.android.redditor;

import java.net.URI;

import com.cd.reddit.json.mapping.RedditLink;

import android.app.Activity;
import android.app.Fragment;
//import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *
 */
public class LinkDisplayFragment extends Fragment {

    private Redditor parentActivity;

    private WebView webview;
    private ProgressBar linkLoadingProgressBar;

    private String url;

    private String subreddit;
    private String linkId;
    private RedditLink theLinkData;


    /**
     *
     */
    public static LinkDisplayFragment newInstance(final String url, final String subreddit, final String linkId) {
        Log.d(GlobalDefines.LOG_TAG, "LinkDisplayFragment: newInstance()");
        Log.v(GlobalDefines.LOG_TAG, "LinkDisplayFragment: newInstance(): subreddit: " + subreddit);
        Log.v(GlobalDefines.LOG_TAG, "LinkDisplayFragment: newInstance(): linkId: " + linkId);

        final LinkDisplayFragment thisFragment = new LinkDisplayFragment();

        final Bundle args = new Bundle();
        args.putString(GlobalDefines.BUNDLE_KEY_FOR_URL, url);
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

        this.url = null;
        try {
            this.url = this.getArguments().getString(GlobalDefines.BUNDLE_KEY_FOR_URL);
        } catch (final NullPointerException npe) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView(): no url in the bundle, so must be the initial instance of " + this.getClass().getSimpleName());
        } catch (final Exception e) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView(): some other exception...", e);
        }

        try {
            this.subreddit = savedInstanceState.getString(GlobalDefines.BUNDLE_KEY_FOR_SUBREDDIT_NAME);
            this.linkId = savedInstanceState.getString(GlobalDefines.BUNDLE_KEY_FOR_LINK_ID);
        } catch (final Exception e) {
            Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView(): exception getting the subreddit name and link id from the Bundle", e);
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

       return inflater.inflate(R.layout.link_display_fragment, container, false);
   }


   /**
    *
    */
   @Override
   public void onViewCreated(final View view, final Bundle savedInstanceState) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onViewCreated()");

       // --- progress bar for page load ---
       this.linkLoadingProgressBar = (ProgressBar) this.getView().findViewById(R.id.link_loading_progress);


       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getting the WebView...");
       this.webview = (WebView) this.getView().findViewById(R.id.link_display);
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getting the WebView settings...");
       final WebSettings settings = this.webview.getSettings();
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": setting the WebView javascript...");
       settings.setJavaScriptEnabled(true);
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": setting the WebView LoadWithOverviewMode...");
       settings.setLoadWithOverviewMode(true);
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": setting the WebView WideViewPort...");
       settings.setUseWideViewPort(true);
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": setting the WebView client...");
       //this.webview.setWebViewClient(new WebViewClient());

       this.webview.setWebChromeClient(new WebChromeClient() {
           private boolean areDisplayingProgress = false;

           @Override
           public void onProgressChanged(final WebView view, final int newProgress) {
               Log.v(GlobalDefines.LOG_TAG, "WebChromeClient: onProgressChanged()");
               Log.v(GlobalDefines.LOG_TAG, "WebChromeClient: onProgressChanged(): new progress: " + Integer.valueOf(newProgress).toString());
               // if we are coming back from a redirect or if we're loading a new Link's url...
               if (newProgress == 0) {
                   // ...reset the progress bar.
                   Log.v(GlobalDefines.LOG_TAG, "WebChromeClient: onProgressChanged(): progress is ZERO. setting to Indeterminate");
                   this.areDisplayingProgress = false;
                   LinkDisplayFragment.this.linkLoadingProgressBar.setIndeterminate(true);
                   LinkDisplayFragment.this.linkLoadingProgressBar.setProgress(newProgress);
               // ...else if we're not yet displaying the progress and the reported progress has passed the threshold...
               } else if (!this.areDisplayingProgress && newProgress > GlobalDefines.PROGRESS_LOADING_SHOW_THRESHOLD - 1) {
                       Log.v(GlobalDefines.LOG_TAG, "WebChromeClient: onProgressChanged(): progress above threshold. setting Indeterminate OFF");
                       this.areDisplayingProgress = true;
                       LinkDisplayFragment.this.linkLoadingProgressBar.setIndeterminate(false);
                       LinkDisplayFragment.this.linkLoadingProgressBar.setProgress(newProgress);
               // ...else we are set to display the progress...
               } else {
                   // ...so display the progress.
                   Log.v(GlobalDefines.LOG_TAG, "WebChromeClient: onProgressChanged(): setting progress (and that's it)");
                   LinkDisplayFragment.this.linkLoadingProgressBar.setProgress(newProgress);
               }
           }

       });
       this.webview.setWebViewClient(new WebViewClient() {

           @Override
           public boolean shouldOverrideUrlLoading(final WebView view, final String urlNewString) {
               Log.v(GlobalDefines.LOG_TAG, "WebViewClient: shouldOverrideUrlLoading()");

               view.loadUrl(urlNewString);
               return true;
           }
       });


       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView(): attempting to set the url...");
       if (this.url == null) {
           Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onCreateView(): attempting to call loadDataWithBaseURL()...");
           this.webview.loadDataWithBaseURL(null, "<html></html>", "text/html", "utf-8", null);
       } else {
           this.webview.loadUrl(this.url);
       }

       // --- Comments Button ---
       final TextView commentsButton = (TextView) this.getView().findViewById(R.id.comments_button);
       commentsButton.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(final View v) {
               Log.d(GlobalDefines.LOG_TAG, "commentsButton.onClick()");

               Log.v(GlobalDefines.LOG_TAG, "commentsButton.onClick(): subreddit: " + LinkDisplayFragment.this.subreddit);
               Log.v(GlobalDefines.LOG_TAG, "commentsButton.onClick(): link id: " + LinkDisplayFragment.this.linkId);

               LinkDisplayFragment.this.parentActivity.displayCommentsFragment(LinkDisplayFragment.this.theLinkData);
           }
       });

   }


   /**
    *
    */
   public void loadNewUrl(final String newUrl) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl()");

       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): newUrl: " + newUrl);
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): currentUrl: " + this.url);


       try {
           // if the new url is a valid one...
           final URI uri = URI.create(newUrl);
       } catch (final IllegalArgumentException iae) {
           // if the url wasn't valid... load a blank page.
           this.blankTheDisplay();
           return;
       }




       // --- Determine if we've been told to load the same url as we already have ---
       boolean isNewUrl = false;
       try {
           // if the new url is different than the current one...
           if (!this.url.equals(newUrl)) {
               // ...blank the webview...
               Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): blanking the webview due to new url");
               this.blankTheDisplay();
               // ...and set to load the new one.
               isNewUrl = true;
               this.linkLoadingProgressBar.setProgress(0);
               this.linkLoadingProgressBar.setIndeterminate(true);
               Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): setting progress 0 and to Indeterminate");
           }
       } catch (final Exception e) {
           Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): error determining if reloading url", e);
           isNewUrl = true;
       }

       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewUrl(): isNewUrl: " + Boolean.valueOf(isNewUrl).toString());

       if (isNewUrl) {
           // ...load the new url.
           this.webview.loadUrl(newUrl);
           this.url = newUrl;
       }

   }

   /**
    *
    */
   public void loadNewLink(final RedditLink linkData) {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewLink()");

       this.theLinkData = linkData;
       this.subreddit = this.theLinkData.getSubreddit();
       this.linkId = this.theLinkData.getId();
       Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": loadNewlink(): subreddit: " + this.subreddit + " - link id: " + this.linkId);

       final String linkUrl = this.theLinkData.getUrl();
       URI theLinkUri = null;
       try {
           theLinkUri = URI.create(linkUrl);
       } catch (final IllegalArgumentException iae) {
           Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": onItemClick(): " + linkUrl + " is not a proper url");
           return;
       }

       this.loadNewUrl(linkUrl);
   }


   /**
    *
    */
   public void blankTheDisplay() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": blankTheDisplay()");

       this.webview.loadDataWithBaseURL(null, "<html></html>", "text/html", "utf-8", null);
       this.linkLoadingProgressBar.setIndeterminate(false);
       this.linkLoadingProgressBar.setProgress(100);
   }


   /**
    *
    */
   protected String getCurrentUrl() {
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getCurrentUrl()");
       Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getCurrentUrl(): current url: " + this.url);

       return this.url;
   }

}
