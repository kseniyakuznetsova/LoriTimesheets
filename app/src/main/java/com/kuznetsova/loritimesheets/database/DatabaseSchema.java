package com.kuznetsova.loritimesheets.database;

import android.provider.BaseColumns;

public class DatabaseSchema {

    public static abstract class TimeEntryEntity implements BaseColumns {
        public static final String TABLE_NAME = "time_entry";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME_IN_MINUTES = "time_in_minutes";
        public static final String COLUMN_NAME_TIME_IN_HOURS= "time_in_hours";
        public static final String COLUMN_NAME_DESCRIPTION= "description";
        public static final String COLUMN_NAME_NAME_TASK= "name_task";
        public static final String COLUMN_NAME_NAME_PROJECT= "name_project";
        public static final String COLUMN_NAME_ID_USER= "id_user";
    }
}
