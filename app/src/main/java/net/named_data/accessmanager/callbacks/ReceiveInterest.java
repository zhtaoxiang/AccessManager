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
package net.named_data.accessmanager.callbacks;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.named_data.accessmanager.util.Common;
import net.named_data.jndn.Data;
import net.named_data.jndn.Exclude;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.encrypt.GroupManager;
import net.named_data.jndn.encrypt.GroupManagerDb;
import net.named_data.jndn.encrypt.Schedule;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.util.Blob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * this class handles E-KEY interest, D-KEY catalog interest and D-KEY interest
 * (1) E-KEY interest is sent by NAC producer, it is of the format:
 *     /org/openmhealth/<user-id>/READ/<data-type>/E-KEY with exclude and childselector
 *     (org/openmhealth/<user-id>/READ/<data-type>/E-KEY/<start-timepoint>/<end-timepoint> is also possible,
 *     but it's not sent by NAC producer)
 * (2) D-KEY catalog interest is sent by DSU, it is of the format:
 *     /org/openmhealth/<user-id>/READ/<data-type>/D-KEY/catalog/<start-timepoint>/<end-timepoint>
 * (3) D-KEY interest is of the format:
 *     /org/openmhealth/<user-id>/READ/<data-type>/D-KEY/<start-timepoint>/<end-timepoint>/FOR/<some consumer>
 */
public class ReceiveInterest implements OnInterestCallback {
  private static final String TAG = "ReceiveInterest";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private Map<String, GroupManager> prefixAccessManagerMap;

  public ReceiveInterest(Map<String, GroupManager> prefixAccessManagerMap) {
    this.prefixAccessManagerMap = prefixAccessManagerMap;
  }

  @Override
  public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {
    Log.d(TAG, "receive interest: " + interest.toUri());
    String nameStartingFromDataType = interest.getName().getSubName(Common.DATA_TYPE_START_INDEX).toUri();
    // The first portion deals with E-KEY interest
    // The first element is the dataType prefix
    // The second element is one of the followings
    // (1)/<start-timepoint>/<end-timepoint>
    // (2)/<start-timepoint>
    // (3)empty
    if(nameStartingFromDataType.contains(Common.EKEY)) {
      String[] dataTypeAndTimestamp = nameStartingFromDataType.split(Common.EKEY);
      GroupManager gm = prefixAccessManagerMap.get(dataTypeAndTimestamp[0]);
      if(gm == null)
        return;
      String timepoint;
      if(dataTypeAndTimestamp.length == 1) {
        Exclude exclude = interest.getExclude();
        int childSelector = interest.getChildSelector();
        // case (3)
        // for now, only check the E-KEY interest sent by NAC producer
        if(exclude.size() == 2 && exclude.get(1).getType() == Exclude.Type.ANY &&
          exclude.get(0).getType() == Exclude.Type.COMPONENT
          && childSelector == 1) {
          timepoint = exclude.get(0).getComponent().toEscapedString();
          Log.d(TAG, "get exclude");
        } else {
          // TODO: deal with other cases
          Log.d(TAG, "get nothing");
          return;
        }
      } else {
        // case (1) or (2)
        timepoint = dataTypeAndTimestamp[1].substring(1, 1 + Common.TIMESTAMP_LEN);
      }
      try {
        List groupKeys = gm.getGroupKey(Schedule.fromIsoString(timepoint));
        face.putData((Data)groupKeys.get(0));
        Log.d(TAG, ((Data) groupKeys.get(0)).getName().toUri());
      } catch (IOException | EncodingException | SecurityException | GroupManagerDb.Error e) {
        e.printStackTrace();
      }
    } else if(nameStartingFromDataType.contains(Common.DKEY)) {
      // The second portion deals with D-KEY catalog interest
      // /org/openmhealth/<user-id>/READ/<data-type>/D-KEY/catalog/<start-timepoint>/<end-timepoint>
      String[] dataTypeAndTimestamp = null;
      if (nameStartingFromDataType.contains(Common.CATALOG)) {
        dataTypeAndTimestamp = nameStartingFromDataType.split(Common.DKEY + Common.CATALOG);
      } else {
        dataTypeAndTimestamp = nameStartingFromDataType.split(Common.DKEY);
      }
      GroupManager gm = prefixAccessManagerMap.get(dataTypeAndTimestamp[0]);
      if(gm == null)
        return;
      String timepoint = dataTypeAndTimestamp[1].substring(1, 1 + Common.TIMESTAMP_LEN);
      try {
        System.out.println("here");
        List groupKeys = gm.getGroupKey(Schedule.fromIsoString(timepoint));
        if (nameStartingFromDataType.contains(Common.CATALOG)) {
          Data returnedData = new Data();
          // name
          returnedData.setName(interest.getName());
          // content
          List<String> dKeyNames = new ArrayList<>();
          for(int i = 1; i < groupKeys.size(); i ++) {
            dKeyNames.add(((Data)(groupKeys.get(i))).getName().toUri());
          }
          String content = objectMapper.writeValueAsString(dKeyNames);
          returnedData.setContent(new Blob(content));
          // signature
          Common.keyChain.sign(returnedData);
          face.putData(returnedData);
          System.out.println("here");
        } else {
          // The third portion deals with D-KEY interest
          // /org/openmhealth/<user-id>/READ/<data-type>/D-KEY/<start-timepoint>/<end-timepoint>/FOR/<some consumer>
          for(int i = 1; i < groupKeys.size(); i ++) {
            Data oneDKey = (Data)(groupKeys.get(i));
            if (oneDKey.getName().equals(interest.getName())) {
              face.putData(oneDKey);
              return;
            }
          }
        }
      } catch (IOException | EncodingException | SecurityException | GroupManagerDb.Error e) {
        e.printStackTrace();
      }
    }
    // ignore other cases
    // TODO: fill this in later
  }
}
