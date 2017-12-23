package com.kuznetsova.loritimesheets.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kuznetsova.loritimesheets.entity.TimeEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.kuznetsova.loritimesheets.R;

public class TimeEntryInSearchAdapter extends BaseAdapter {
    private Context context;
    private List<TimeEntry> timeEntryList;
    private LayoutInflater inflater;

    public TimeEntryInSearchAdapter(Context context, List<TimeEntry> timeEntryList) {
        this.context = context;
        this.timeEntryList = timeEntryList;
        inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_time_entry_in_search, viewGroup, false);
        final TimeEntry timeEntry = getTimeEntry(position);

        SimpleDateFormat sdfReverse = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM");
        try {
            calendar.setTime(sdfReverse.parse(timeEntry.getDate()));
            ((TextView) convertView.findViewById(R.id.tvDay)).setText(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
            ((TextView) convertView.findViewById(R.id.tvMonth)).setText(sdfMonth.format(calendar.getTime()));
            ((TextView) convertView.findViewById(R.id.tvYear)).setText(Integer.toString(calendar.get(Calendar.YEAR)));
            int hours=(int)timeEntry.getTimeInHours();
            int minutes=timeEntry.getTimeInMinutes()-hours*60;
            ((TextView) convertView.findViewById(R.id.tvDuration)).setText(String.format("%02d", hours) + " ч " +String.format("%02d", minutes) +" мин");
            ((TextView) convertView.findViewById(R.id.tvProject)).setText("("+timeEntry.getTask().getProject().getName()+")");
            ((TextView) convertView.findViewById(R.id.tvTask)).setText(timeEntry.getTask().getName());
            String description=timeEntry.getDescription();
            if(description==null)
                description=context.getString(R.string.empty_field);
            ((TextView) convertView.findViewById(R.id.tvDescription)).setText(" "+description);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public TimeEntry getTimeEntry(int position){
        return ((TimeEntry) getItem(position));
    }

    public List<TimeEntry> getRecordList() {
        return timeEntryList;
    }

    public void setRecordList(List<TimeEntry> timeEntryList) {
        this.timeEntryList = timeEntryList;
    }
}
