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

public class ScheduleDetail {
  private String name;
  // this is actual the datatype prefix, but not the entire prefix
  // /org/openmhealth/haitao/READ/<dataType>
  private String dataType;
  private String startDate;
  private String endDate;
  private int startHour;
  private int endHour;

  public ScheduleDetail(String name, String dataType, String startDate, String endDate,
                        int startHour, int endHour) {
    this.name = name;
    this.dataType = dataType;
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

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
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

  @Override
  public String toString() {
    return "[ name:" + name + ", dataType:" + dataType + ", startDate:" + startDate + ", endDate:"
      + endDate + ", startHour:" + startHour + ", endHour:" + endHour + " ]";
  }
}
