package net.named_data.accessmanager.database;

import java.util.List;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class MembershipDetail {
  private String id;
  private String key;
  private List<String> scheduleList;

  public MembershipDetail(String id, String key, List<String> scheduleList) {
    this.id = id;
    this.key = key;
    this.scheduleList = scheduleList;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public List<String> getScheduleList() {
    return scheduleList;
  }

  public void setScheduleList(List<String> scheduleList) {
    this.scheduleList = scheduleList;
  }
}
