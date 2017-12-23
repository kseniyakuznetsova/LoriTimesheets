package com.kuznetsova.loritimesheets.activity;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.kuznetsova.loritimesheets.R;
import com.kuznetsova.loritimesheets.adapter.WeekPagerAdapter;
import com.kuznetsova.loritimesheets.entity.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WeekTimeEntryActivity extends AppCompatActivity {

    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String NEW_WEEK_CHOSEN = "NEW_WEEK_CHOSEN";
    private static final String SUM_TIME="SUM_TIME";

    private ViewPager weekPager;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvSumTime;

    private User user;
    private WeekPagerAdapter adapter;
    private Calendar calendar;
    private SimpleDateFormat sdfFullDate;
    private SimpleDateFormat sdfMonth;
    private SimpleDateFormat sdfYear;
    private double sumHours=0;
    private int sumMinutes=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_time_entry);

        user = (User) getIntent().getSerializableExtra("user");

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() - 1000);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        sdfFullDate = new SimpleDateFormat("dd.MM.yyy");
        sdfMonth = new SimpleDateFormat("MMM");
        sdfYear = new SimpleDateFormat("yyyy");

        adapter = new WeekPagerAdapter(getSupportFragmentManager());
        adapter.setUser(user);
        adapter.setCalendar(calendar);

        tvSumTime = (TextView) findViewById(R.id.tvSumTime);
        tvStartDate = (TextView) findViewById(R.id.tvStartDate);
        tvEndDate = (TextView) findViewById(R.id.tvEndDate);
        weekPager = (ViewPager) findViewById(R.id.weekPager);

        Calendar calendarForDay = Calendar.getInstance();
        calendarForDay.setTime(calendar.getTime());
        calendarForDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        tvStartDate.setText(sdfFullDate.format(calendarForDay.getTime()));
        calendarForDay.add(Calendar.DAY_OF_WEEK, 6);
        tvEndDate.setText(sdfFullDate.format(calendarForDay.getTime()));

        weekPager.setAdapter(adapter);
        weekPager.setCurrentItem(1);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(SUM_TIME));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onItemSignOutClick(MenuItem item) {
        getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).edit().putString(TOKEN_KEY, null).apply();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void onItemSearchClick(MenuItem item) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("user", (Serializable) user);
        startActivity(intent);
    }

    public void onCalendarClick(View view) {
        new DatePickerDialog(this, dateDialog, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    DatePickerDialog.OnDateSetListener dateDialog = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int newYear, int monthOfYear,
                              int dayOfMonth) {
            calendar.set(Calendar.YEAR, newYear);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            adapter.setCalendar(calendar);

            Calendar calendarForDay = Calendar.getInstance();
            calendarForDay.setTime(calendar.getTime());
            calendarForDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            tvStartDate.setText(sdfFullDate.format(calendarForDay.getTime()));
            calendarForDay.add(Calendar.DAY_OF_WEEK, 6);
            tvEndDate.setText(sdfFullDate.format(calendarForDay.getTime()));

            sumHours=0;
            sumMinutes=0;
            Intent intent = new Intent(NEW_WEEK_CHOSEN);
            LocalBroadcastManager.getInstance(WeekTimeEntryActivity.this).sendBroadcast(intent);
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SUM_TIME)) {
                    sumHours += intent.getDoubleExtra("hours", 0);
                    sumMinutes += intent.getIntExtra("minutes", 0);
                    int hours = (int) sumHours;
                    int minutes = sumMinutes - hours * 60;
                    tvSumTime.setText(String.format("%02d", hours) + " ч " + String.format("%02d", minutes) + " мин");

            }
        }
    };

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("sumHours", 0);
        outState.putInt("sumMinutes", 0);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sumHours = savedInstanceState.getDouble("sumHours");
        sumMinutes = savedInstanceState.getInt("sumMinutes");
    }
}