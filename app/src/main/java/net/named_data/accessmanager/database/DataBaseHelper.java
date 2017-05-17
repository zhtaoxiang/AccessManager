package net.named_data.accessmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
  // if the user chooses a different user id, use a different database
  public static String DATABASE_NAME = null;
  private static final int DATABASE_VERSION = 1;

  public DataBaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(ScheduleTable.SQL_CREATE_TABLE);
    db.execSQL(MembershipTable.SQL_CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO
  }
}
