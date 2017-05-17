package net.named_data.accessmanager.database;

import android.provider.BaseColumns;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public abstract class MembershipTable implements BaseColumns {
  public static final String SCHEDULE_DELIMITER = "\t";
  public static final String TABLE_NAME = "MembershipTable";
  public static final String MEMBER_ID = "member_prefix";
  public static final String MEMBER_KEY = "member_key";
  public static final String SCHEDULE_LIST = "schedule_list";
  public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
    + _ID + " INTEGER PRIMARY KEY, "
    + MEMBER_ID + " TEXT UNIQUE, "
    + MEMBER_KEY + " TEXT, "
    + SCHEDULE_LIST + " TEXT "
    + ")";
}
