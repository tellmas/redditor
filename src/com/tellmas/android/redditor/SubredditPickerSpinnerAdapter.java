package com.tellmas.android.redditor;

import android.app.Activity;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 *
 */
public class SubredditPickerSpinnerAdapter implements SpinnerAdapter {

    private final String[] menuItems;
    private final LayoutInflater inflater;
    //private final Activity activity;
    private final String sortByDisplayText;

    /**
     *
     */
    public SubredditPickerSpinnerAdapter(Activity activity, String[] menuItems, String sortByDisplayText) {

        //this.activity = activity;

        LayoutInflater li = null;
        try {
            li = activity.getLayoutInflater();
        } catch (NullPointerException npe) {
            Log.w(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": SubredditPickerSpinnerAdapter(): 'activity' param was null", npe);
        }
        this.inflater = li;

        this.menuItems = menuItems;
        this.sortByDisplayText = sortByDisplayText;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return this.menuItems.length;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return this.menuItems[position];
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int position) {
        // adapter always returns the same type of View for all items.. so return 0
        return 0;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView()");

        Log.v(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getView(): position: " + position);

        if (convertView == null || !(convertView instanceof ViewGroup)) {
            convertView = (ViewGroup) this.inflater.inflate(R.layout.layout_subreddit_picker_header, parent, false);
        }

        TextView subredditName = (TextView) convertView.findViewById(R.id.subreddit_picker_subreddit);
        subredditName.setText((String) this.getItem(0));

        TextView sortBy = (TextView) convertView.findViewById(R.id.subreddit_picker_sort_by);
        sortBy.setText(this.sortByDisplayText);



        return convertView;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        // adapter always returns the same type of View for all items.. so return 1
        return 1;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        if (this.menuItems.length > 0) {
            return false;
        } else {
            return true;
        }
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#registerDataSetObserver(android.database.DataSetObserver)
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // no observer needed
        return;
    }


    /* (non-Javadoc)
     * @see android.widget.Adapter#unregisterDataSetObserver(android.database.DataSetObserver)
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // no need
        return;
    }


    /* (non-Javadoc)
     * @see android.widget.SpinnerAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Log.d(GlobalDefines.LOG_TAG, this.getClass().getSimpleName() + ": getDropDownView()");

        if (convertView == null || !(convertView instanceof TextView)) {
            convertView = (TextView) this.inflater.inflate(R.layout.layout_subreddit_picker_item, parent, false);
        }
        /*
        Resources resources = this.activity.getResources();
        String item = (String) this.getItem(position);
        int stringResourceId = resources.getIdentifier(item, null, null);
        String subredditName = resources.getString(stringResourceId);
        //TextView view = (TextView) convertView;
        //view.setText(subredditName);
        ((TextView) convertView).setText(subredditName);
        */
        ((TextView) convertView).setText((String) this.getItem(position));

        return convertView;
    }

}
