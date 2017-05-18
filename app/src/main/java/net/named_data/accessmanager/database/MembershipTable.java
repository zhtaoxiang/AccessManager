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

public abstract class MembershipTable implements BaseColumns {
  public static final String SCHEDULE_DELIMITER = "\t";
  public static final String TABLE_NAME = "MembershipTable";
  public static final String MEMBER_NAME = "member_name";
  public static final String MEMBER_ID = "member_id";
  public static final String MEMBER_CERT = "member_cert";
  public static final String SCHEDULE_LIST = "schedule_list";
  public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
    + _ID + " INTEGER PRIMARY KEY, "
    + MEMBER_NAME + " TEXT UNIQUE, "
    + MEMBER_ID + " TEXT UNIQUE, "
    + MEMBER_CERT + " TEXT, "
    + SCHEDULE_LIST + " TEXT "
    + ")";
}
