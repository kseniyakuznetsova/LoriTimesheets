package com.kuznetsova.loritimesheets.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kuznetsova.loritimesheets.entity.TimeEntry;
import com.kuznetsova.loritimesheets.R;

import java.util.List;

public class TimeEntryAdapter extends BaseAdapter {

    private Context context;
    private List<TimeEntry> timeEntryList;
    private LayoutInflater inflater;

    public TimeEntryAdapter(Context context, List<TimeEntry> timeEntryList) {
        this.context = context;
        this.timeEntryList = timeEntryList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return timeEntryList.size();
    }

    @Override
    public Object getItem(int position) {
        return timeEntryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_time_entry, parent, false);
        }

        final TimeEntry timeEntry = getTimeEntry(position);
        int hours = (int) timeEntry.getTimeInHours();
        int minutes = timeEntry.getTimeInMinutes() - hours * 60;
        ((TextView) convertView.findViewById(R.id.tvDuration)).setText(" " + String.format("%02d", hours) + " ч " +String.format("%02d", minutes) + " мин");
        ((TextView) convertView.findViewById(R.id.tvProject)).setText(" " + timeEntry.getTask().getProject().getName());
        ((TextView) convertView.findViewById(R.id.tvTask)).setText(" " + timeEntry.getTask().getName());
        return convertView;
    }

    public TimeEntry getTimeEntry(int position) {
        return ((TimeEntry) getItem(position));
    }

    public List<TimeEntry> getTimeEntryList() {
        return timeEntryList;
    }

    public void setTimeEntryList(List<TimeEntry> timeEntryList) {
        this.timeEntryList = timeEntryList;
    }
}
