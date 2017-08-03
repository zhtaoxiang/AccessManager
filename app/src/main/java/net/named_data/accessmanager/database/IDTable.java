package net.named_data.accessmanager.database;

import android.provider.BaseColumns;

/**
 * Created by zhtaoxiang on 7/30/17.
 */

public class IDTable implements BaseColumns {
  public static final String TABLE_NAME = "IDTable";
  public static final String APP_ID = "app_id";
  public static final String APP_CERT_NAME = "app_cert_name";
  public static final String SIGNER_ID = "signer_id";
  public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
    + _ID + " INTEGER PRIMARY KEY autoincrement, "
    + APP_ID + " TEXT, "
    + APP_CERT_NAME + " TEXT, "
    + SIGNER_ID + " TEXT "
    + ")";
}
