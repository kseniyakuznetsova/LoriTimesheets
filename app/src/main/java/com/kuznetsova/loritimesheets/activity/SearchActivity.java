package com.kuznetsova.loritimesheets.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kuznetsova.loritimesheets.API.APIFactory;
import com.kuznetsova.loritimesheets.API.APIService;
import com.kuznetsova.loritimesheets.R;
import com.kuznetsova.loritimesheets.Utils;
import com.kuznetsova.loritimesheets.adapter.TimeEntryInSearchAdapter;
import com.kuznetsova.loritimesheets.database.DatabaseService;
import com.kuznetsova.loritimesheets.entity.TimeEntry;
import com.kuznetsova.loritimesheets.entity.User;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private final static String TIME_ENTRY_LIST_FOR_SEARCH = "TIME_ENTRY_LIST_FOR_SEARCH";

    private TextView tvCurrentDate;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvNoInformation;
    private EditText etSearchText;
    private ListView lvTimeEntry;
    private ProgressDialog progressDialog;
    private TimeEntryInSearchAdapter timeEntryAdapter;

    private SimpleDateFormat sdfDate;
    private SimpleDateFormat sdfDateReverse;
    private Calendar calendarStart;
    private Calendar calendarEnd;
    private User user;
    private List<TimeEntry> timeEntryList;
    private APIService service;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        user=(User)getIntent().getSerializableExtra("user");
        service = APIFactory.getAPIService();
        token = getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null);
        timeEntryList=new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        tvStartDate=(TextView) findViewById(R.id.tvStartDate);
        tvEndDate=(TextView) findViewById(R.id.tvEndDate);
        etSearchText=(EditText) findViewById(R.id.etSearchText);
        tvNoInformation=(TextView) findViewById(R.id.tvNoInformation);
        lvTimeEntry = (ListView) findViewById(R.id.lvTimeEntry);

        timeEntryAdapter = new TimeEntryInSearchAdapter(getApplicationContext(), timeEntryList);
        lvTimeEntry.setAdapter(timeEntryAdapter);
        lvTimeEntry.setEmptyView(tvNoInformation);

        sdfDate =  new SimpleDateFormat("dd.MM.yyyy");
        sdfDateReverse=new SimpleDateFormat("yyyy-MM-dd");

        calendarStart = Calendar.getInstance();
        calendarStart.setTimeInMillis(System.currentTimeMillis());
        calendarEnd = Calendar.getInstance();
        calendarEnd.setTimeInMillis(System.currentTimeMillis());

        tvEndDate.setText(sdfDate.format(calendarEnd.getTime()));
        tvStartDate.setText(sdfDate.format(calendarStart.getTime()));
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,  new IntentFilter(TIME_ENTRY_LIST_FOR_SEARCH));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onClickSearch(View view){
        try {
            Date dateStart = sdfDate.parse(tvStartDate.getText().toString());
            Date dateEnd = sdfDate.parse(tvEndDate.getText().toString());
            if(dateStart.getTime()>dateEnd.getTime())
                Toast.makeText(getBaseContext(), getString(R.string.error_date), Toast.LENGTH_SHORT).show();
            else {
                if(Utils.isNetworkAvailable(this)) {
                    timeEntryList.clear();
                    JsonObject jo = new JsonObject();
                    JsonArray ja = new JsonArray();
                    ja.add(user.getId());
                    jo.add("userId", ja);
                    jo.addProperty("dateStart", sdfDateReverse.format(dateStart));
                    jo.addProperty("dateEnd", sdfDateReverse.format(dateEnd));
                    jo.addProperty("searchString", "%" + etSearchText.getText().toString().toUpperCase().trim() + "%");
                    progressDialog.setMessage(getString(R.string.find_information));
                    progressDialog.show();
                    final Call<List<TimeEntry>> timeCall = service.findTimeEntry(jo, "Bearer " + token);
                    timeCall.enqueue(new Callback<List<TimeEntry>>() {
                        @Override
                        public void onResponse(Call<List<TimeEntry>> call, Response<List<TimeEntry>> response) {
                            if (response.isSuccessful()) {
                                timeEntryList = response.body();
                                progressDialog.dismiss();
                                if (timeEntryList.size() == 0) {
                                    Toast.makeText(SearchActivity.this, getString(R.string.error_not_found), Toast.LENGTH_SHORT).show();
                                }
                                timeEntryAdapter = new TimeEntryInSearchAdapter(getApplicationContext(), timeEntryList);
                                lvTimeEntry.setAdapter(timeEntryAdapter);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<TimeEntry>> call, Throwable t) {
                            Toast.makeText(SearchActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Intent intent = new Intent(this, DatabaseService.class);
                    intent.putExtra("dateStart", sdfDateReverse.format(calendarStart.getTime()));
                    intent.putExtra("dateEnd", sdfDateReverse.format(calendarEnd.getTime()));
                    intent.putExtra("userId",user.getId());
                    intent.putExtra("searchString","%"+etSearchText.getText().toString()+"%");
                    intent.setAction("findTimeEntry");
                    startService(intent);
                }


            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void onClickStartDate(View view) {
        tvCurrentDate = (TextView) findViewById(R.id.tvStartDate);
        new DatePickerDialog(this, dateDialogStart, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void onClickEndDate(View view) {
        tvCurrentDate = (TextView) findViewById(R.id.tvEndDate);
        new DatePickerDialog(this, dateDialogEnd, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH), calendarEnd.get(Calendar.DAY_OF_MONTH)).show();
    }
    DatePickerDialog.OnDateSetListener dateDialogStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int newYear, int monthOfYear,
                              int dayOfMonth) {
            calendarStart.set(Calendar.YEAR, newYear);
            calendarStart.set(Calendar.MONTH, monthOfYear);
            calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvCurrentDate.setText(sdfDate.format(calendarStart.getTime()));
        }
    };
    DatePickerDialog.OnDateSetListener dateDialogEnd = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int newYear, int monthOfYear,
                              int dayOfMonth) {
            calendarEnd.set(Calendar.YEAR, newYear);
            calendarEnd.set(Calendar.MONTH, monthOfYear);
            calendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvCurrentDate.setText(sdfDate.format(calendarEnd.getTime()));
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(TIME_ENTRY_LIST_FOR_SEARCH)){
                timeEntryList=(List<TimeEntry>) intent.getSerializableExtra("timeEntryList");
                timeEntryAdapter = new TimeEntryInSearchAdapter(getApplicationContext(), timeEntryList);
                lvTimeEntry.setAdapter(timeEntryAdapter);
            }
        }
    };

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("timeEntryList",(Serializable) timeEntryList);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeEntryList = (List<TimeEntry>)savedInstanceState.getSerializable("timeEntryList");
    }
}
