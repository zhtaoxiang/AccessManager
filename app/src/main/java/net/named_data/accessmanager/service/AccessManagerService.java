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

/**
 * Created by zhtaoxiang on 5/15/17.
 */

public class AccessManagerService extends Service {
  private static final String TAG = "AccessManagerService";

  public static String prefix = "/org/openmhealth/zhehao/READ";

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
        addMembers(db.getAllMembers());
      } catch (Exception e) {
        e.printStackTrace();
      }
      // register prefix to accept incoming Interest
      faceCommandExecutor.execute(new Runnable() {
        @Override
        public void run() {
          try {
            m_face.registerPrefix(new Name(prefix), new ReceiveInterest(), new RegisterFailed());
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

  public void addOneSchedule(ScheduleDetail scheduleDetail) throws Exception {
    if(scheduleDetail == null)
      return;
    if(scheduleNameAccessManagerMap.get(scheduleDetail.getName()) != null) {
      throw new Exception("Cannot add a schedule with an existing name!");
    }
    // check whether the manager exists or not. if not, create a new one
    GroupManager gm = prefixAccessManagerMap.get(scheduleDetail.getPrefix());
    if (gm == null) {
      try {
        gm = new GroupManager(
          new Name(Common.userPrefix), // user prefix
          new Name(scheduleDetail.getPrefix()), //data type
          new AndroidSqlite3GroupManagerDb(getApplicationContext().getFilesDir().getAbsolutePath()
            + "/" + Common.MANAGE_DB_NAME), // database
          Common.KEY_SIZE, Common.KEY_FRESHNESS_HOURS, Common.keyChain);
        prefixAccessManagerMap.put(scheduleDetail.getPrefix(), gm);
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    // add a schedule
    Schedule schedule = new Schedule();
    try {
      RepetitiveInterval interval = new RepetitiveInterval(
        Schedule.fromIsoString(scheduleDetail.getStartDate()),
        Schedule.fromIsoString(scheduleDetail.getEndDate()),
        scheduleDetail.getStartHour(), scheduleDetail.getEndHour(), 1,
        RepetitiveInterval.RepeatUnit.DAY);
      schedule.addWhiteInterval(interval);
      gm.addSchedule(scheduleDetail.getName(), schedule);
      scheduleNameAccessManagerMap.put(scheduleDetail.getName(), gm);
    } catch (EncodingException | GroupManagerDb.Error e) {
      e.printStackTrace();
    }
  }

  public void addSchedules(List<ScheduleDetail> scheduleDetailList) throws Exception {
    if (scheduleDetailList == null || scheduleDetailList.isEmpty())
      return;
    for(ScheduleDetail one : scheduleDetailList) {
      addOneSchedule(one);
    }
  }

  public void addOneMember(final MembershipDetail membershipDetail) throws Exception {
    if (membershipDetail == null)
      return;
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
          interest.setName(new Name(membershipDetail.getKey()));
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
