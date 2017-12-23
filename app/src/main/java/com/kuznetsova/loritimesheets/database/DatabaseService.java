package com.kuznetsova.loritimesheets.database;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.kuznetsova.loritimesheets.entity.TimeEntry;

import java.io.Serializable;
import java.util.List;

public class DatabaseService extends IntentService {
    private final static String TIME_ENTRY_LIST_FOR_DAY = "TIME_ENTRY_LIST_FOR_DAY";
    private final static String TIME_ENTRY_LIST_FOR_SEARCH="TIME_ENTRY_LIST_FOR_SEARCH";

    private DatabaseHelper dbHelper;

    public DatabaseService() {
        super("DatabaseService");
    }

    public DatabaseService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "insertList":
                    dbHelper.deleteTimeEntryByDay(intent.getStringExtra("date"));
                    List<TimeEntry> timeEntryList = ( List<TimeEntry>) intent.getSerializableExtra("timeEntryList");
                    for(TimeEntry timeEntry:timeEntryList)
                        dbHelper.insertTimeEntry(timeEntry);
                    break;
                case "getAllOfDay":
                    List<TimeEntry> timeEntryAllList = dbHelper.getAllTimeEntryOfDay(intent.getStringExtra("date"),intent.getStringExtra("userId"));
                    Intent intentToActivity = new Intent(TIME_ENTRY_LIST_FOR_DAY);
                    intentToActivity.putExtra("timeEntryList", (Serializable) timeEntryAllList);
                    intentToActivity.putExtra("date",intent.getStringExtra("date"));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToActivity);
                    break;
                case "findTimeEntry":
                    List<TimeEntry> timeEntryFindList = dbHelper.findTimeEntry(intent.getStringExtra("dateStart"),intent.getStringExtra("dateEnd"),intent.getStringExtra("userId"),intent.getStringExtra("searchString"));
                    Intent intentToSearchActivity = new Intent(TIME_ENTRY_LIST_FOR_SEARCH);
                    intentToSearchActivity.putExtra("timeEntryList", (Serializable) timeEntryFindList);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentToSearchActivity);
                    break;
            }

            dbHelper.close();
        }

    }
}
