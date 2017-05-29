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

import net.named_data.accessmanager.util.EntityInfo;

import java.util.Arrays;
import java.util.List;

public class MembershipDetail {
  private String name;
  private String id;
  private String cert;
  private List<String> scheduleList;

  public MembershipDetail(String name, String id, String cert, List<String> scheduleList) {
    this.name = name;
    this.id = id;
    this.cert = cert;
    this.scheduleList = scheduleList;
  }

  public MembershipDetail(EntityInfo entityInfo, List<String> scheduleList) {
    this.name = entityInfo.getName();
    this.id = entityInfo.getId();
    this.cert = entityInfo.getCert();
    this.scheduleList = scheduleList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCert() {
    return cert;
  }

  public void setCert(String cert) {
    this.cert = cert;
  }

  public List<String> getScheduleList() {
    return scheduleList;
  }

  public void setScheduleList(List<String> scheduleList) {
    this.scheduleList = scheduleList;
  }

  @Override
  public String toString() {
    String[] temp = new String[scheduleList.size()];
    scheduleList.toArray(temp);
    return "[ name: " + name + ", id:" + id + ", key:" + cert + ", scheduleList:(" + Arrays.toString(temp) + ") ]";
  }
}
