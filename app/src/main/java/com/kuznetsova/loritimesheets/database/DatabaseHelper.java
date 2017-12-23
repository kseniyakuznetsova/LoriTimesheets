package com.kuznetsova.loritimesheets.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kuznetsova.loritimesheets.entity.Project;
import com.kuznetsova.loritimesheets.entity.Task;
import com.kuznetsova.loritimesheets.entity.TimeEntry;
import com.kuznetsova.loritimesheets.entity.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "myLoriDB";
    private static final int DB_VERSION = 1;
    private static final String CREATE_TABLE_TIME_ENTRY = "create table time_entry ("
            + "_id text primary key,"
            + "date text,"
            + "description text,"
            + "time_in_minutes text,"
            + "time_in_hours text,"
            + "id_user text,"
            + "name_task text,"
            + "name_project text);";
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_TIME_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public List<TimeEntry> getAllTimeEntryOfDay(String date, String userId) {
        List<TimeEntry> timeEntryList = new ArrayList<TimeEntry>();
        String selectQuery = "SELECT  * FROM " + DatabaseSchema.TimeEntryEntity.TABLE_NAME
                +" WHERE "+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_ID_USER +"=?"
                +" AND "+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE+"=?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,new String[]{userId,date});

        if (cursor.moveToFirst()) {
            do {
                TimeEntry timeEntry = new TimeEntry();
                timeEntry.setId(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity._ID)));
                timeEntry.setDate(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE)));
                timeEntry.setTimeInMinutes(cursor.getInt(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_MINUTES)));
                timeEntry.setTimeInHours(cursor.getDouble(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_HOURS)));
                timeEntry.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DESCRIPTION)));
                timeEntry.setUser(new User(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_ID_USER))));
                Project project=new Project();
                project.setName(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_PROJECT)));
                Task task=new Task();
                task.setName(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_TASK)));
                task.setProject(project);
                timeEntry.setTask(task);
                timeEntryList.add(timeEntry);
            } while (cursor.moveToNext());
        }
        return timeEntryList;
    }

    public List<TimeEntry> findTimeEntry(String dateStart, String dateEnd,String userId, String searchString) {
        List<TimeEntry> timeEntryList = new ArrayList<TimeEntry>();
        String selectQuery = "SELECT  * FROM " + DatabaseSchema.TimeEntryEntity.TABLE_NAME
                +" WHERE "+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_ID_USER +"=?"
                +" AND ("+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE+">=?"
                +" AND "+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE+"<=?)"
                +" AND (TRIM(LOWER("+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_TASK+")) like LOWER(?)"
                +" OR TRIM(LOWER("+DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DESCRIPTION+")) like LOWER(?))" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId,dateStart,dateEnd,searchString.trim().toLowerCase(),searchString.trim().toLowerCase()});

        if (cursor.moveToFirst()) {
            do {
                TimeEntry timeEntry = new TimeEntry();
                timeEntry.setId(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity._ID)));
                timeEntry.setDate(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE)));
                timeEntry.setTimeInMinutes(cursor.getInt(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_MINUTES)));
                timeEntry.setTimeInHours(cursor.getDouble(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_HOURS)));
                timeEntry.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DESCRIPTION)));
                timeEntry.setUser(new User(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_ID_USER))));
                Project project=new Project();
                project.setName(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_PROJECT)));
                Task task=new Task();
                task.setName(cursor.getString(cursor.getColumnIndex(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_TASK)));
                task.setProject(project);
                timeEntry.setTask(task);
                timeEntryList.add(timeEntry);
            } while (cursor.moveToNext());
        }
        return timeEntryList;
    }

    public void insertTimeEntry(TimeEntry timeEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseSchema.TimeEntryEntity._ID, timeEntry.getId());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE, timeEntry.getDate());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_MINUTES, timeEntry.getTimeInMinutes());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_TIME_IN_HOURS, timeEntry.getTimeInHours());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DESCRIPTION, timeEntry.getDescription());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_TASK, timeEntry.getTask().getName());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_NAME_PROJECT, timeEntry.getTask().getProject().getName());
        values.put(DatabaseSchema.TimeEntryEntity.COLUMN_NAME_ID_USER, timeEntry.getUser().getId());

        db.insertOrThrow(DatabaseSchema.TimeEntryEntity.TABLE_NAME, null, values);
    }

    public void deleteTimeEntryByDay(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseSchema.TimeEntryEntity.TABLE_NAME, DatabaseSchema.TimeEntryEntity.COLUMN_NAME_DATE + " = ?",
                new String[]{String.valueOf(date)});
    }
}
