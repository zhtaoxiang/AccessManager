package net.named_data.accessmanager;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import net.named_data.accessmanager.database.DataBaseHelper;
import net.named_data.accessmanager.service.AccessManagerService;
import net.named_data.accessmanager.util.Common;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class MyApp extends Application {
  private static final String TAG = "MyApp";

  @Override
  public void onCreate() {
    super.onCreate();
    DataBaseHelper.DATABASE_NAME = Common.userPrefix.split("/")[3] + ".db";
    Log.d(TAG, "userPrefix is " + Common.userPrefix);
    Log.d(TAG, "database name is " + DataBaseHelper.DATABASE_NAME);
    // start the background service when the app is launched
    startService(new Intent(this, AccessManagerService.class));
  }

}
