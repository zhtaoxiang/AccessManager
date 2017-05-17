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
package net.named_data.accessmanager.database;

import android.provider.BaseColumns;

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
