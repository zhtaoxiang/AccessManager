package net.named_data.accessmanager.database;

/**
 * Created by zhtaoxiang on 5/16/17.
 */

public class ScheduleDetail {
  private String name;
  // this is actual the datatype prefix, but not the entire prefix
  // /org/openmhealth/haitao/READ/<prefix>
  private String prefix;
  private String startDate;
  private String endDate;
  private int startHour;
  private int endHour;

  public ScheduleDetail(String name, String prefix, String startDate, String endDate,
                        int startHour, int endHour) {
    this.name = name;
    this.prefix = prefix;
    this.startDate = startDate;
    this.endDate = endDate;
    this.startHour = startHour;
    this.endHour = endHour;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public int getStartHour() {
    return startHour;
  }

  public void setStartHour(int startHour) {
    this.startHour = startHour;
  }

  public int getEndHour() {
    return endHour;
  }

  public void setEndHour(int endHour) {
    this.endHour = endHour;
  }
}
