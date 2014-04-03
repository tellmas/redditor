package com.tellmas.android.redditor;

import java.util.concurrent.TimeUnit;

import android.app.Application;

/**
 * Global constants for this app.
 */
public final class GlobalDefines extends Application {

    /**
     * the "tag" for android.util.Log
     */
    public static final String LOG_TAG = "REDDITOR";


    /**
     *
     */
    public static final String USER_AGENT = "fetchit/0.5 by tellmas";



    //  ************************ api constants ************************
    /**
     *
     */
    public static final String REDDIT_URI_API = "http://api.reddit.com/";
    public static final String REDDIT_API_LISTING_DATAYPE_JSON = ".json";
    public static final String REDDIT_API_LISTING_DATAYPE_XML = ".xml";
    public static final String REDDIT_API_LISTING_DATAYPE_DEFAULT = REDDIT_API_LISTING_DATAYPE_JSON;
    /**
     *
     */
    public static final String REDDIT_API_LISTING_DEFAULT = "hot.json";



    //  ************************ Bundle keys ************************
    public static final String BUNDLE_KEY_LIST_OF_LINKS = "bundlekeylistoflinks";
    public static final String BUNDLE_KEY_FOR_URL = "bundlekeyforurl";


    // ==== ====
    public static final String SUBREDDIT_URI_PREFIX = "/r/";

    // ==== ====
    public static final int EXIT_STATUS_ERROR = 1;

    // ==== ====
    public static final double SCROLL_DURATION_FACTOR = 2; // multiple of slower
    // ==== ====
    public static final int PROGRESS_LOADING_SHOW_THRESHOLD = 10;


    // ===== ======
    public static final String DEFAULT_LISTING = "";
    public static final String DEFAULT_SORT = "hot";


    public enum RedditorTimeUnit {SECONDS, MINUTES, HOURS, DAYS};
    /**
     *
     * @param seconds TODO
     * @return TODO
     */
    public static RedditorTime convertToAppropriateTimeUnits(final RedditorTime seconds) {

        final RedditorTime time = seconds;
        final long timeValue = seconds.getTimeValue();

        if (TimeUnit.SECONDS.toDays(timeValue) > 0) {
            time.setTimeValue(TimeUnit.SECONDS.toDays(timeValue));
            time.setTimeUnit(RedditorTimeUnit.DAYS);

        } else if (TimeUnit.SECONDS.toHours(timeValue) > 0) {
            time.setTimeValue(TimeUnit.SECONDS.toHours(timeValue));
            time.setTimeUnit(RedditorTimeUnit.HOURS);

        } else if (TimeUnit.SECONDS.toMinutes(timeValue) > 0) {
            time.setTimeValue(TimeUnit.SECONDS.toMinutes(timeValue));
            time.setTimeUnit(RedditorTimeUnit.MINUTES);
        }


        return time;
    }


    // ===== class instantiation =====
    private static GlobalDefines singleton;
    public static GlobalDefines getInstance() {
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
