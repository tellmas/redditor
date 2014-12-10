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


    /**
     * TODO
     */
    public static String submissionTimeStringBuilder(String submissionTime, Context context) throws NumberFormatException {
        return submissionTimeStringBuilder(Long.parseLong(submissionTime), context);
    }


    /**
     * TODO
     */
    public static String submissionTimeStringBuilder(long submissionTime, Context context) {

        final StringBuilder submissionTimeSB = new StringBuilder();
        RedditorTime timeAgo = new RedditorTime(
                System.currentTimeMillis() / 1000 - submissionTime
                ,RedditorTimeUnit.SECONDS
        );
        timeAgo = GlobalDefines.convertToAppropriateTimeUnits(timeAgo);
        submissionTimeSB.append(Long.toString(timeAgo.getTimeValue()));
        submissionTimeSB.append(" ");
        boolean isSingularValue = false;
        if (timeAgo.getTimeValue() == 1) {
            isSingularValue = true;
        }
        int timeUnitId = 0;
        switch(timeAgo.getTimeUnit()) {
            case SECONDS:
                if (isSingularValue) {
                    timeUnitId = R.string.second;
                } else {
                    timeUnitId = R.string.seconds;
                }
                break;
            case MINUTES:
                if (isSingularValue) {
                    timeUnitId = R.string.minute;
                } else {
                    timeUnitId = R.string.minutes;
                }
                break;
            case HOURS:
                if (isSingularValue) {
                    timeUnitId = R.string.hour;
                } else {
                    timeUnitId = R.string.hours;
                }
                break;
            case DAYS:
                if (isSingularValue) {
                    timeUnitId = R.string.day;
                } else {
                    timeUnitId = R.string.days;
                }
                break;
            default:
                timeUnitId = R.string.empty;
                break;
        }
        submissionTimeSB.append(context.getResources().getString(timeUnitId));
        submissionTimeSB.append(" ");
        submissionTimeSB.append(context.getResources().getString(R.string.ago));
        return submissionTimeSB.toString();
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
