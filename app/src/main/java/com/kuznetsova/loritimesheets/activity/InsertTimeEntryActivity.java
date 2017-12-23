package com.kuznetsova.loritimesheets.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kuznetsova.loritimesheets.API.APIFactory;
import com.kuznetsova.loritimesheets.API.APIService;
import com.kuznetsova.loritimesheets.R;
import com.kuznetsova.loritimesheets.Utils;
import com.kuznetsova.loritimesheets.entity.Project;
import com.kuznetsova.loritimesheets.entity.ProjectParticipant;
import com.kuznetsova.loritimesheets.entity.Task;
import com.kuznetsova.loritimesheets.entity.TimeEntry;
import com.kuznetsova.loritimesheets.entity.User;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsertTimeEntryActivity extends AppCompatActivity {

    private static final String TOKEN_PREFERENCE = "TOKEN_PREFERENCE";
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private final static int SUCCESSFUL_INSERT_NEW_TIME_ENTRY = 1;

    private TextView tvAction;
    private TextView tvTask;
    private TextView tvProject;
    private NumberPicker npHours;
    private NumberPicker npMinutes;
    private EditText etDescription;
    private ProgressDialog progressDialog;

    private APIService service;

    private List<Project> projectList;
    private List<String> projectNameList;
    private List<Task> taskList;
    private List<String> taskNameList;
    private User user;
    private TimeEntry timeEntry;
    private Task task;
    private Calendar calendar;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_time_entry);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        tvAction = (TextView) findViewById(R.id.tvAction);
        tvProject = (TextView) findViewById(R.id.tvProject);
        tvTask = (TextView) findViewById(R.id.tvTask);
        npHours = (NumberPicker) findViewById(R.id.npDurationHours);
        npMinutes = (NumberPicker) findViewById(R.id.npDurationMinutes);
        etDescription = (EditText) findViewById(R.id.etDescription);

        projectList = new ArrayList<>();
        projectNameList = new ArrayList<>();

        user = (User) getIntent().getSerializableExtra("user");
        timeEntry = (TimeEntry) getIntent().getSerializableExtra("timeEntry");
        if (timeEntry != null) { //обновление
           tvAction.setText(getString(R.string.update_title));
            tvTask.setText(timeEntry.getTask().getName());
            tvProject.setText(timeEntry.getTask().getProject().getName());
            if (timeEntry.getDescription() != null)
                etDescription.setText(timeEntry.getDescription());
            int hours = (int) timeEntry.getTimeInHours();
            int minutes = timeEntry.getTimeInMinutes() - hours * 60;
            npHours.setValue(hours);
            npMinutes.setValue(minutes);
            tvTask.setEnabled(true);
            task = timeEntry.getTask();
            calendar = Calendar.getInstance();
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            try {
                calendar.setTime(sdfDate.parse(timeEntry.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {//добавление
            tvAction.setText(getString(R.string.create_title));
            calendar = (Calendar) getIntent().getSerializableExtra("date");
        }

        service = APIFactory.getAPIService();
        token = getSharedPreferences(TOKEN_PREFERENCE, Context.MODE_PRIVATE).getString(TOKEN_KEY, null);
        if (Utils.isNetworkAvailable(this)) {
            JsonObject jo = new JsonObject();
            JsonArray ja = new JsonArray();
            ja.add(user.getId());
            jo.add("userId", ja);
            final Call<List<ProjectParticipant>> projectCall = service.getProjectList(jo, "Bearer " + token);
            projectCall.enqueue(new Callback<List<ProjectParticipant>>() {
                @Override
                public void onResponse(Call<List<ProjectParticipant>> call, Response<List<ProjectParticipant>> response) {
                    if (response.isSuccessful()) {
                        List<ProjectParticipant> pp = response.body();
                        for (ProjectParticipant projectParticipant : pp) {
                            projectList.add(projectParticipant.getProject());
                            projectNameList.add(projectParticipant.getProject().getName());
                            if(timeEntry != null)
                                getTaskList(task.getProject().getId());
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<ProjectParticipant>> call, Throwable t) {
                    Toast.makeText(InsertTimeEntryActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.right_menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_save:
                if (tvProject.getText().toString().equals(getString(R.string.not_check)))
                    Toast.makeText(this, R.string.error_project_not_chosen, Toast.LENGTH_SHORT).show();
                else if (tvTask.getText().toString().equals(getString(R.string.not_check)))
                    Toast.makeText(this, R.string.error_task_not_chosen, Toast.LENGTH_SHORT).show();
                else if (npHours.getValue() == 0 && npMinutes.getValue() == 0)
                    Toast.makeText(this, R.string.error_duration, Toast.LENGTH_SHORT).show();
                else {
                    if (Utils.isNetworkAvailable(this)) {
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = calendar.getTime();
                        final Call<TimeEntry> timeEntryCall;
                        if (timeEntry == null)//добавление
                        {
                            timeEntry = new TimeEntry(user, sdfDate.format(date), task, npMinutes.getValue(), npHours.getValue(), etDescription.getText().toString());
                            timeEntryCall = service.createTimeEntry(timeEntry, "Bearer " + token);
                        } else//изменение
                        {
                            timeEntry = new TimeEntry(timeEntry.getId(), user, sdfDate.format(date), task, npMinutes.getValue(), npHours.getValue(), etDescription.getText().toString());
                            timeEntryCall = service.updateTimeEntry(timeEntry.getId(), timeEntry, "Bearer " + token);
                        }
                        progressDialog.setMessage(getString(R.string.saving_data));
                        progressDialog.show();
                        timeEntryCall.enqueue(new Callback<TimeEntry>() {
                            @Override
                            public void onResponse(Call<TimeEntry> call, Response<TimeEntry> response) {
                                if (response.isSuccessful()) {
                                    progressDialog.dismiss();
                                    setResult(SUCCESSFUL_INSERT_NEW_TIME_ENTRY);
                                    InsertTimeEntryActivity.super.onBackPressed();
                                }
                            }

                            @Override
                            public void onFailure(Call<TimeEntry> call, Throwable t) {
                                progressDialog.dismiss();
                                Toast.makeText(InsertTimeEntryActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else
                        Toast.makeText(this, R.string.network_failed, Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickChooseProject(View view) {
        if (Utils.isNetworkAvailable(this)) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setTitle(getString(R.string.title_choose_project));

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, projectNameList);

            builderSingle.setNegativeButton(getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tvProject.setText(arrayAdapter.getItem(which));
                    getTaskList(projectList.get(which).getId());
                }
            });
            builderSingle.show();
        } else
            Toast.makeText(this, R.string.network_failed, Toast.LENGTH_SHORT).show();
    }

    public void onClickChooseTask(View view) {
        if (Utils.isNetworkAvailable(this)) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setTitle(getString(R.string.title_choose_task));

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, taskNameList);

            builderSingle.setNegativeButton(getString(R.string.dialog_btn_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tvTask.setText(arrayAdapter.getItem(which));
                    task = taskList.get(which);
                }
            });
            builderSingle.show();
        } else
            Toast.makeText(this, R.string.network_failed, Toast.LENGTH_SHORT).show();
    }
    private void getTaskList(String projectId){
        progressDialog.setMessage(getString(R.string.getting_data));
        progressDialog.show();
        taskList = new ArrayList<Task>();
        taskNameList = new ArrayList<String>();
        tvTask.setEnabled(true);
        JsonObject joCondition = new JsonObject();
        joCondition.addProperty("property", "project");
        joCondition.addProperty("operator", "=");
        joCondition.addProperty("value",projectId);
        JsonArray jaConditions = new JsonArray();
        jaConditions.add(joCondition);
        JsonObject joFilter = new JsonObject();
        joFilter.add("conditions", jaConditions);
        JsonObject joBody = new JsonObject();
        joBody.add("filter", joFilter);
        final Call<List<Task>> projectCall = service.getTaskList(joBody, "Bearer " + token);
        projectCall.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    taskList = response.body();
                    for (Task task : taskList) {
                        taskNameList.add(task.getName());
                    }
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(InsertTimeEntryActivity.this, R.string.network_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
