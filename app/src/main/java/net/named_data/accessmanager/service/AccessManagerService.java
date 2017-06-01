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
package net.named_data.accessmanager.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import net.named_data.accessmanager.callbacks.ReceiveInterest;
import net.named_data.accessmanager.callbacks.RegisterFailed;
import net.named_data.accessmanager.database.DataBase;
import net.named_data.accessmanager.database.MembershipDetail;
import net.named_data.accessmanager.database.ScheduleDetail;
import net.named_data.accessmanager.util.Common;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.encoding.der.DerDecodingException;
import net.named_data.jndn.encrypt.AndroidSqlite3GroupManagerDb;
import net.named_data.jndn.encrypt.GroupManager;
import net.named_data.jndn.encrypt.GroupManagerDb;
import net.named_data.jndn.encrypt.RepetitiveInterval;
import net.named_data.jndn.encrypt.Schedule;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static net.named_data.accessmanager.util.Common.DATA_TYPE_PREFIXES_TO_DB_MAP;

public class AccessManagerService extends Service {
  private static final String TAG = "AccessManagerService";

  public static boolean isRunning = false;
  private final IBinder mBinder = new LocalBinder();
  private Face m_face;
  private DataBase db;
  private ScheduledThreadPoolExecutor faceEventProcessExecutor = new ScheduledThreadPoolExecutor(1);
  private ScheduledThreadPoolExecutor faceCommandExecutor = new ScheduledThreadPoolExecutor(1);

  private Map<String, GroupManager> prefixAccessManagerMap = new HashMap<>();
  private Map<String, GroupManager> scheduleNameAccessManagerMap = new HashMap<>();

  @Override
  public void onCreate() {
    m_face = new Face("localhost");
    db = DataBase.getInstance(getApplicationContext());
    try {
      m_face.setCommandSigningInfo(Common.keyChain, Common.keyChain.getDefaultCertificateName());
    }
    catch (SecurityException e) {
      // shouldn't really happen
      /// @Todo add logging
    }
  }
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if(!isRunning) {
      Log.d(TAG, "onStartCommand()");
      // modify the indicating variable
      isRunning = true;
      // preload existing managers
      try {
        addSchedules(db.getAllSchedules());
//        addMembers(db.getAllMembers());
      } catch (Exception e) {
        e.printStackTrace();
      }
      // register prefix to accept incoming Interest
      faceCommandExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            Log.d(TAG, "register preifx: " + Common.accessControlPrefix);
            m_face.registerPrefix(new Name(Common.accessControlPrefix),
              new ReceiveInterest(prefixAccessManagerMap), new RegisterFailed());
          } catch (IOException | SecurityException e) {
            e.printStackTrace();
          }
        }
      });
      // start to process events
      faceEventProcessExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            while(true) {
              m_face.processEvents();
              Thread.sleep(5);
            }
          } catch (IOException | EncodingException| InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
    }
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    if(!isRunning) {
      startService(new Intent(this, AccessManagerService.class));
    }
    return mBinder;
  }

  //returns the instance of the service
  public class LocalBinder extends Binder {
    public AccessManagerService getServiceInstance(){
      return AccessManagerService.this;
    }
  }

  // stop the service
  @Override
  public void onTaskRemoved (Intent rootIntent){
    Log.d(TAG, "AccessManagerService::onTaskRemoved()");
    faceEventProcessExecutor.shutdownNow();
    faceCommandExecutor.shutdown();
    m_face.shutdown();
    this.stopSelf();
  }

  public void addOneSchedule(ScheduleDetail scheduleDetail, boolean isAddedUsingUI) throws Exception {

    if(scheduleDetail == null)
      return;
    Log.d(TAG, "add schedule: " + scheduleDetail.toString());
    if(scheduleNameAccessManagerMap.get(scheduleDetail.getName()) != null) {
      throw new Exception("Cannot add a schedule with an existing name!");
    }
    // check whether the manager exists or not. if not, create a new one
    GroupManager gm = prefixAccessManagerMap.get(scheduleDetail.getDataType());
    if (gm == null) {
      try {
        gm = new GroupManager(
          new Name(Common.userPrefix), // user prefix
          new Name(scheduleDetail.getDataType()), //data type
          new AndroidSqlite3GroupManagerDb(getApplicationContext().getFilesDir().getAbsolutePath()
            + "/" + DATA_TYPE_PREFIXES_TO_DB_MAP.get(scheduleDetail.getDataType())),
          // TODO: the database should have a user specific prefix
          Common.KEY_SIZE, Common.KEY_FRESHNESS_HOURS, Common.keyChain);
        prefixAccessManagerMap.put(scheduleDetail.getDataType(), gm);
        scheduleNameAccessManagerMap.put(scheduleDetail.getName(), gm);
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    // add a schedule: this adds a schedule to the database
    if(isAddedUsingUI) {
      Schedule schedule = new Schedule();
      try {
        RepetitiveInterval interval = new RepetitiveInterval(
          Schedule.fromIsoString(scheduleDetail.getStartDate() + Common.DATE_SUFFIX),
          Schedule.fromIsoString(scheduleDetail.getEndDate() + Common.DATE_SUFFIX),
          scheduleDetail.getStartHour(), scheduleDetail.getEndHour(), 1,
          RepetitiveInterval.RepeatUnit.DAY);
        schedule.addWhiteInterval(interval);
        gm.addSchedule(scheduleDetail.getName(), schedule);
      } catch (EncodingException | GroupManagerDb.Error e) {
        e.printStackTrace();
      }
    }
  }

  public void addSchedules(List<ScheduleDetail> scheduleDetailList) throws Exception {
    if (scheduleDetailList == null || scheduleDetailList.isEmpty())
      return;
    for(ScheduleDetail one : scheduleDetailList) {
      addOneSchedule(one, false);
    }
  }

  public void addOneMember(final MembershipDetail membershipDetail) throws Exception {

    if (membershipDetail == null)
      return;
    Log.d(TAG, "add member: " + membershipDetail.toString());
    for(final String scheduleName : membershipDetail.getScheduleList()) {
      final GroupManager gm = scheduleNameAccessManagerMap.get(scheduleName);
      if(gm == null) {
        throw new Exception("Schedule " + scheduleName + " doesn't exist");
      }
      // fetch key and add member
      faceCommandExecutor.execute(new Runnable() {
        @Override
        public void run() {
          Interest interest = new Interest();
          interest.setName(new Name(membershipDetail.getCert()));
          try {
            m_face.expressInterest(interest,
              new OnData() {
                @Override
                public void onData(Interest interest, Data data) {
                  try {
                    gm.addMember(scheduleName, data);
                  } catch (DerDecodingException | GroupManagerDb.Error e) {
                    e.printStackTrace();
                  }
                }
              }, new OnTimeout() {
                @Override
                public void onTimeout(Interest interest) {
                  //TODO: if failed, try it again (but when?)
                  Log.d(TAG, "failed to fetch the member's certificate");
                }
              });
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public void addMembers(List<MembershipDetail> membershipDetailList) throws Exception {
    if (membershipDetailList == null || membershipDetailList.isEmpty())
      return;
    for(MembershipDetail one : membershipDetailList) {
      addOneMember(one);
    }
  }
}
