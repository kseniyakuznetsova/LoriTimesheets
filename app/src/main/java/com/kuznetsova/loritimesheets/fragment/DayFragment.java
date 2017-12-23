package com.kuznetsova.loritimesheets.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kuznetsova.loritimesheets.API.APIFactory;
import com.kuznetsova.loritimesheets.API.APIService;
import com.kuznetsova.loritimesheets.Utils;
import com.kuznetsova.loritimesheets.activity.InsertTimeEntryActivity;
import com.kuznetsova.loritimesheets.adapter.TimeEntryAdapter;
import com.kuznetsova.loritimesheets.database.DatabaseService;
import com.kuznetsova.loritimesheets.entity.TimeEntry;
import com.kuznetsova.loritimesheets.entity.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.kuznetsova.loritimesheets.R;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayFragment extends Fragment {
    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";

    private final static String TIME_ENTRY_LIST_FOR_DAY = "TIME_ENTRY_LIST_FOR_DAY";
    private static final String SUM_TIME = "SUM_TIME";
    private final static int ADD_NEW_TIME_ENTRY = 0;
    private final static int SUCCESSFUL_ADD_NEW_TIME_ENTRY = 1;

    private User user;
    private List<TimeEntry> timeEntryList;
    private Calendar calendar;
    private APIService service;
    private String token;

    private View view;
    private ImageView ivInsert;
    private TextView tvDay;
    private TextView tvMonth;
    private TextView tvYear;
    private TextView tvNameOfDayOfWeek;
    private ListView lvTimeEntry;

    public DayFragment() {
    }

    public static DayFragment newInstance(Calendar dayDate, User user) {
        Bundle bundle = new Bundle(1);
        bundle.putSerializable("day", dayDate);
        bundle.putSerializable("user", user);

        DayFragment fragment = new DayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getArguments().getSerializable("user");
        calendar = (Calendar) getArguments().getSerializable("day");
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        service = APIFactory.getAPIService();
        timeEntryList = new ArrayList<>();

        token = getActivity().getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null);

        view = inflater.inflate(R.layout.fragment_day, container, false);
        tvDay = (TextView) view.findViewById(R.id.tvDay);
        tvMonth = (TextView) view.findViewById(R.id.tvMonth);
        tvYear = (TextView) view.findViewById(R.id.tvYear);
        tvNameOfDayOfWeek = (TextView) view.findViewById(R.id.tvNameOfDayOfWeek);

        lvTimeEntry = (ListView) view.findViewById(R.id.lvTimeEntry);
        lvTimeEntry.setEmptyView((TextView) view.findViewById(R.id.tvNoInformation));

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM");
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfNameOfDayOfWeek = new SimpleDateFormat("EEEE");
        String dayOfWeek = sdfNameOfDayOfWeek.format(calendar.getTime());
        tvNameOfDayOfWeek.setText(dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1));
        tvDay.setText(sdfDay.format(calendar.getTime()));
        tvMonth.setText(sdfMonth.format(calendar.getTime()));
        tvYear.setText(sdfYear.format(calendar.getTime()));

        ivInsert = (ImageView) view.findViewById(R.id.ivInsert);
        ivInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getContext())) {
                    Intent intent = new Intent(getActivity(), InsertTimeEntryActivity.class);
                    intent.putExtra("user", (Serializable) user);
                    intent.putExtra("date", (Serializable) calendar);
                    startActivityForResult(intent, ADD_NEW_TIME_ENTRY);
                } else
                    Toast.makeText(getActivity(), R.string.network_failed, Toast.LENGTH_SHORT).show();
            }
        });

        lvTimeEntry.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long id) {
                onClickListView(position);
                return true;
            }
        });
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(TIME_ENTRY_LIST_FOR_DAY));

        refreshTimeEntryList();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_TIME_ENTRY && resultCode == SUCCESSFUL_ADD_NEW_TIME_ENTRY) {
            refreshTimeEntryList();
        }
    }

    private void refreshTimeEntryList() {
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Date date = calendar.getTime();
        JsonObject jo = new JsonObject();
        JsonArray ja = new JsonArray();
        ja.add(user.getId());
        jo.add("userId", ja);
        jo.addProperty("dateFind", sdfDate.format(date));
        if (Utils.isNetworkAvailable(getContext())) {
            final Call<List<TimeEntry>> timeEntryCall = service.getTimeEntryOfDay(jo, "Bearer " + token);
            timeEntryCall.enqueue(new Callback<List<TimeEntry>>() {
                @Override
                public void onResponse(Call<List<TimeEntry>> call, Response<List<TimeEntry>> response) {
                    if (response.isSuccessful()) {
                        timeEntryList = response.body();
                        Intent intent = new Intent(getActivity(), DatabaseService.class);
                        intent.putExtra("date", sdfDate.format(calendar.getTime()));
                        intent.setAction("insertList");
                        intent.putExtra("timeEntryList", (Serializable) timeEntryList);
                        getActivity().startService(intent);
                        TimeEntryAdapter timeEntryAdapter = new TimeEntryAdapter(getActivity().getApplicationContext(), timeEntryList);
                        lvTimeEntry.setAdapter(timeEntryAdapter);
                        justifyListViewHeightBasedOnChildren(lvTimeEntry);
                        calculateSumTime(getContext());
                    }
                }

                @Override
                public void onFailure(Call<List<TimeEntry>> call, Throwable t) {
                }
            });
        } else {
            Intent intent = new Intent(getActivity(), DatabaseService.class);
            intent.putExtra("date", sdfDate.format(calendar.getTime()));
            intent.putExtra("userId", user.getId());
            intent.setAction("getAllOfDay");
            getActivity().startService(intent);
        }
    }

    public static void justifyListViewHeightBasedOnChildren(ListView listView) {

        TimeEntryAdapter adapter = (TimeEntryAdapter) listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    public void onClickListView(final int position) {
        final TimeEntryAdapter timeEntryAdapter = (TimeEntryAdapter) lvTimeEntry.getAdapter();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        String[] typesAction = getResources().getStringArray(R.array.context_menu_action);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, typesAction);

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (Utils.isNetworkAvailable(getContext())) {
                            TimeEntry timeEntry = timeEntryAdapter.getTimeEntryList().get(position);
                            Intent intent = new Intent(getActivity(), InsertTimeEntryActivity.class);
                            intent.putExtra("timeEntry", (Serializable) timeEntry);
                            intent.putExtra("user", (Serializable) user);
                            startActivityForResult(intent, ADD_NEW_TIME_ENTRY);
                        } else
                            Toast.makeText(getActivity(), R.string.network_failed, Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        if (Utils.isNetworkAvailable(getContext())) {
                            String id = timeEntryAdapter.getTimeEntryList().get(position).getId();
                            final Call<Void> call = service.deleteTimeEntry(id, "Bearer " + token);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        refreshTimeEntryList();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                }
                            });
                        } else
                            Toast.makeText(getActivity(), R.string.network_failed, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builderSingle.show();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            if (intent.getAction().equals(TIME_ENTRY_LIST_FOR_DAY) && intent.getStringExtra("date").equals(sdfDate.format(calendar.getTime()))) {
                timeEntryList = (List<TimeEntry>) intent.getSerializableExtra("timeEntryList");
                TimeEntryAdapter timeEntryAdapter = new TimeEntryAdapter(context, timeEntryList);
                lvTimeEntry.setAdapter(timeEntryAdapter);
                justifyListViewHeightBasedOnChildren(lvTimeEntry);
                calculateSumTime(context);
            }
        }
    };

    private void calculateSumTime(Context context){
        Intent intentToActivity = new Intent(SUM_TIME);
        double sumHours = 0;
        int sumMinutes = 0;
        for (TimeEntry t : timeEntryList) {
            sumHours += t.getTimeInHours();
            sumMinutes += t.getTimeInMinutes();
        }
        intentToActivity.putExtra("hours", sumHours);
        intentToActivity.putExtra("minutes", sumMinutes);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentToActivity);
    }

}
