package net.named_data.accessmanager.database;

import android.provider.BaseColumns;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public abstract class ScheduleTable implements BaseColumns {
  public static final String TABLE_NAME = "ScheduleTable";
  public static final String SCHEDULE_NAME = "schedule_name";
  public static final String PREFIX = "prefix";
  public static final String START_DATE = "start_date";
  public static final String END_DATE = "end_date";
  public static final String START_HOUR = "start_hour";
  public static final String END_HOUR = "end_hour";
  public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
    + _ID + " INTEGER PRIMARY KEY, "
    + SCHEDULE_NAME + " TEXT UNIQUE, "
    + PREFIX + " TEXT, "
    + START_DATE + " TEXT, "
    + END_DATE + " TEXT, "
    + START_HOUR + " INTEGER, "
    + END_HOUR + " INTEGER "
    + ")";
}
