package com.kuznetsova.loritimesheets.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kuznetsova.loritimesheets.entity.User;
import com.kuznetsova.loritimesheets.fragment.WeekFragment;

import java.util.Calendar;

public class WeekPagerAdapter extends FragmentPagerAdapter {
    private User user;
    private Calendar calendar;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public WeekPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return  WeekFragment.newInstance( user,calendar);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
