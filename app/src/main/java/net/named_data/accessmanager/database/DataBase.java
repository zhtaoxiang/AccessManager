package net.named_data.accessmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class DataBase {
  private DataBaseHelper dbHelper;
  private static DataBase instance;

  public static DataBase getInstance(Context context) {
    if(instance == null) {
      instance = new DataBase(context);
    }
    return instance;
  }

  private DataBase(Context contex) {
    dbHelper = new DataBaseHelper(contex);
  }

  public List<ScheduleDetail> getAllSchedules() {
    List<ScheduleDetail> result = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String[] projection = {
      ScheduleTable.SCHEDULE_NAME,
      ScheduleTable.PREFIX,
      ScheduleTable.START_DATE,
      ScheduleTable.END_DATE,
      ScheduleTable.START_HOUR,
      ScheduleTable.END_HOUR
    };

    Cursor cursor = db.query(
      ScheduleTable.TABLE_NAME,
      projection,                               // The columns to return
      null,                                     // The columns for the WHERE clause
      null,                                     // The values for the WHERE clause
      null,                                     // don't group the rows
      null,                                     // don't filter by row groups
      null                                      // The sort order
    );

    if (cursor.moveToFirst()) {
      do {
        result.add(new ScheduleDetail(cursor.getString(0),
          cursor.getString(1),
          cursor.getString(2),
          cursor.getString(3),
          cursor.getInt(4),
          cursor.getInt(5)));
      } while (cursor.moveToNext());
    }
    return result;
  }

  public List<MembershipDetail> getAllMembers() {
    List<MembershipDetail> result = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String[] projection = {
      MembershipTable.MEMBER_ID,
      MembershipTable.MEMBER_KEY,
      MembershipTable.SCHEDULE_LIST
    };

    Cursor cursor = db.query(
      MembershipTable.TABLE_NAME,
      projection,                               // The columns to return
      null,                                     // The columns for the WHERE clause
      null,                                     // The values for the WHERE clause
      null,                                     // don't group the rows
      null,                                     // don't filter by row groups
      null                                      // The sort order
      );

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        // Adding contact to list
        List<String> scheduleList = Arrays.asList(cursor.getString(2).split(MembershipTable.SCHEDULE_DELIMITER));
        result.add(new MembershipDetail(cursor.getString(0), cursor.getString(1), scheduleList));
      } while (cursor.moveToNext());
    }
    return result;
  }

  public void insertSchedule(ScheduleDetail scheduleDetail) {
    ContentValues values = new ContentValues();
    values.put(ScheduleTable.SCHEDULE_NAME, scheduleDetail.getName());
    values.put(ScheduleTable.PREFIX, scheduleDetail.getPrefix());
    values.put(ScheduleTable.START_DATE, scheduleDetail.getStartDate());
    values.put(ScheduleTable.END_DATE, scheduleDetail.getEndDate());
    values.put(ScheduleTable.START_HOUR, scheduleDetail.getStartHour());
    values.put(ScheduleTable.END_HOUR, scheduleDetail.getEndHour());
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.insert(ScheduleTable.TABLE_NAME, null, values);
  }

  public void insertMember(MembershipDetail membershipDetail) {
    ContentValues values = new ContentValues();
    values.put(MembershipTable.MEMBER_ID, membershipDetail.getId());
    values.put(MembershipTable.MEMBER_KEY, membershipDetail.getKey());
    StringBuilder sb = new StringBuilder();
    List<String> scheduleList = membershipDetail.getScheduleList();
    if ((scheduleList != null) && (!scheduleList.isEmpty())) {
      for (int i = 0; i < scheduleList.size() - 1; i ++) {
        sb.append(scheduleList.get(i)).append(MembershipTable.SCHEDULE_DELIMITER);
      }
      sb.append(scheduleList.get(scheduleList.size() - 1));
    }
    values.put(MembershipTable.SCHEDULE_LIST, sb.toString());
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.insert(MembershipTable.TABLE_NAME, null, values);
  }
}
