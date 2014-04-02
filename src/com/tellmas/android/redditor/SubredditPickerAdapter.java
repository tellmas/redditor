package com.tellmas.android.redditor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 *
 */
public class SubredditPickerAdapter<T> extends ArrayAdapter<T> {

    private final String sortByDisplayText;
    private final LayoutInflater inflater;

    public SubredditPickerAdapter(final Context context, final int layoutId, final T[] strings, final String sortByDisplayText) {
        super(context, layoutId, strings);
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": SubredditPickerAdapter() constructor");

        this.sortByDisplayText = sortByDisplayText;

        LayoutInflater li = null;
        try {
            li = ((Activity) context).getLayoutInflater();
        } catch (final ClassCastException cce) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": SubredditPickerAdapter(): 'context' was not an Activity", cce);
        } catch (final NullPointerException npe) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": SubredditPickerAdapter(): 'context' param was null", npe);
        }
        catch (final Exception e) {
            Log.e(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": SubredditPickerAdapter(): some exception", e);
        }
        this.inflater = li;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        //Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView()");

        //Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): position: " + position);

        if (convertView == null || !(convertView instanceof ViewGroup)) {
            convertView = (ViewGroup) this.inflater.inflate(R.layout.layout_subreddit_picker_header, parent, false);
        }

        final TextView subredditName = (TextView) convertView.findViewById(R.id.subreddit_picker_subreddit);
        subredditName.setText((String) this.getItem(0));

        final TextView sortBy = (TextView) convertView.findViewById(R.id.subreddit_picker_sort_by);
        sortBy.setText(this.sortByDisplayText);



        return convertView;
    }


    /* (non-Javadoc)
     * @see android.widget.SpinnerAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getDropDownView()");
        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getDropDownView(): position: " + position);

        return super.getDropDownView(position, convertView, parent);
    }

}
