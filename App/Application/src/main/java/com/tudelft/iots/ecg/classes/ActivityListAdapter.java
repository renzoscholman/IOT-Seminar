package com.tudelft.iots.ecg.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tudelft.iots.ecg.R;
import com.tudelft.iots.ecg.database.model.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityListAdapter extends BaseAdapter {
    private ArrayList<Activity> mActivities;
    private LayoutInflater mInflator;

    static class ViewHolder {
        TextView date;
        TextView times;
        TextView zone;
    }

    public ActivityListAdapter(LayoutInflater inflater) {
        super();
        mActivities = new ArrayList<Activity>();
        mInflator = inflater;
    }

    public void addActivity(Activity activity) {
        if(!mActivities.contains(activity)) {
            mActivities.add(activity);
        }
    }

    public Activity getActivity(int position) {
        return mActivities.get(position);
    }

    public void clear() {
        mActivities.clear();
    }

    @Override
    public int getCount() {
        return mActivities.size();
    }

    @Override
    public Object getItem(int i) {
        return mActivities.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        DateFormatter mFormat = new DateFormatter();
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_activity, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) view.findViewById(R.id.date);
            viewHolder.times = (TextView) view.findViewById(R.id.times);
            viewHolder.zone = (TextView) view.findViewById(R.id.zone);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Activity activity = mActivities.get(i);
        final int zone = activity.getMostUsedZone();
        final int seconds = activity.getTimeinZone(zone);
        viewHolder.times.setText(mFormat.format(activity.timestamp_start, activity.timestamp_end));
        viewHolder.date.setText(mFormat.formatDate(activity.timestamp_start));
        List<Integer> zones = new HeartRateZones(25).getZones();
        String zoneHR = "";
        if(zone > 0){
            zoneHR += zones.get(zone-1) + " < ";
        }
        zoneHR += "HR < " + zones.get(zone);
        viewHolder.zone.setText("Most time spent in zone "+zone+" ("+zoneHR+"): "+seconds+"s");

        return view;
    }
}