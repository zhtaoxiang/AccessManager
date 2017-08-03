/* -*- Mode:jde; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2017 Regents of the University of California
 *
 * This file is part of NDNFit (NDN fitness) Access Manager.
 * See AUTHORS.md for complete list of NDNFit Access Manage authors and contributors.
 *
 * NDNFit Access Manager is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NDNFit Access Manage is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NDNFit Access Manage, e.g., in COPYING file.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.named_data.accessmanager;

import android.app.Application;

public class MyApp extends Application {
  private static final String TAG = "MyApp";

  @Override
  public void onCreate() {
    super.onCreate();
//    DataBaseHelper.DATABASE_NAME = Common.userPrefix.split("/")[3] + ".db";
//    Log.d(TAG, "userPrefix is " + Common.userPrefix);
//    Log.d(TAG, "database name is " + DataBaseHelper.DATABASE_NAME);

    // start the background service when the app is launched
//    startService(new Intent(this, AccessManagerService.class));
  }

}
