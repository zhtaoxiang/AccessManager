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
package net.named_data.accessmanager.util;

import android.content.Context;
import android.util.Log;

import net.named_data.accessmanager.MainActivity;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;

import java.util.HashMap;
import java.util.Map;

public abstract class Common {
  private static final String TAG = "Common";
  public static final int KEY_SIZE = 2048;
  public static final int KEY_FRESHNESS_HOURS = 24 * 365;
  public static final String DATE_SUFFIX = "T000000";

  public static Context mCtx;
  public static String mAppID = "";
  public static Name mAppCertificateName;

  // these 3 variables should be gotten from id manager
  public static String userPrefix = "/org/openmhealth/haitao";
  public static String accessControlPrefix = userPrefix + "/READ";
  public static KeyChain keyChain = configureKeyChain();


  public static void setUserPrefix(Name prefix) {
    Log.d(TAG, "new user prefix is " + prefix.toString());
    userPrefix = prefix.toString();
    accessControlPrefix = userPrefix + "/READ";
  }

  // Data types
  public static final String[] DATA_TYPES = new String[]{
    "All Data",
    "Mobile Caputured Data",
    "DPU processed Data"};
  // data type prefixes
  public static final String[] DATA_TYPE_PREFIXES = new String[] {
    "/fitness",                                      // all data
    "/fitness/physical_activity/time_location",      // Mobile Caputured Data
    "/fitness/physical_activity/processed_result"    // DPU processed Data
  };
  // database name for access managers
  // notice that when use the data name, a use
  public static final Map<String, String> DATA_TYPE_PREFIXES_TO_DB_MAP;
  static
  {
    DATA_TYPE_PREFIXES_TO_DB_MAP = new HashMap<>();
    DATA_TYPE_PREFIXES_TO_DB_MAP.put("/fitness", "_all_data.db");
    DATA_TYPE_PREFIXES_TO_DB_MAP.put("/fitness/physical_activity/time_location", "_mobile_data.db");
    DATA_TYPE_PREFIXES_TO_DB_MAP.put("/fitness/physical_activity/processed_result", "_dpu_data.db");
  }


  public static final String[] PREDEFINED_ENTITY_NAMES = new String[]{"DPU", "DVU"};

  // Currently, the user is only allowed to authorize DPU and DVU to fetch and process his/her data
  public static final Map<String, EntityInfo> PREDEFINED_ENTITY_NAME_MAP;
  static
  {
    PREDEFINED_ENTITY_NAME_MAP = new HashMap<>();
    PREDEFINED_ENTITY_NAME_MAP.put("DPU", new EntityInfo("DPU", "/org/openmhealth/dpu", "/org/openmhealth/dpu/KEY"));
    PREDEFINED_ENTITY_NAME_MAP.put("DVU", new EntityInfo("DVU", "/org/openmhealth/dvu", "/org/openmhealth/dvu/KEY"));
  }

  public static final int DATA_TYPE_START_INDEX = 4;
  public static final String EKEY = "/E-KEY";
  public static final String DKEY = "/D-KEY";
  public static final String CATALOG = "/catalog";
  public static final int TIMESTAMP_LEN = 15;

  /////////////////////////////////////////////////////////////////////////////////////////////////
  private static KeyChain
  configureKeyChain() {
    final MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
    final MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
    final KeyChain _keyChain = new KeyChain(new IdentityManager(identityStorage, privateKeyStorage),
      new SelfVerifyPolicyManager(identityStorage));

    Name name = new Name("/tmp-identity");

    try {
      // create keys, certs if necessary
      if (!identityStorage.doesIdentityExist(name)) {
        _keyChain.createIdentityAndCertificate(name);

        // set default identity
        _keyChain.getIdentityManager().setDefaultIdentity(name);
      }
    }
    catch (SecurityException e){
      // shouldn't really happen
      /// @Todo add logging
    }

    return _keyChain;
  }

  public static void setAppID(String appID, Name certName) {
    mAppID = appID;
    mAppCertificateName = new Name(certName);

    if (mAppID != null && !mAppID.isEmpty()) {
      String dbPath = mCtx.getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
      String certDirPath = mCtx.getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

      IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
      PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
      IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);


      // For now, the verification policy manager, and the face to fetch required cert does not matter
      // So we use SelfVerifyPolicyManager, and don't call KeyChain.setFace()
      keyChain = new KeyChain(identityManager, new SelfVerifyPolicyManager(identityStorage));
      try {
        Log.d(TAG, "the default certificate is " + keyChain.getDefaultCertificateName().toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void setAppID(String appID, IdentityCertificate certificate) {
    mAppID = appID;
    mAppCertificateName = new Name(certificate.getName());

    if (mAppID != null && !mAppID.isEmpty()) {
      String dbPath = mCtx.getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
      String certDirPath = mCtx.getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

      IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
      PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
      IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);


      // For now, the verification policy manager, and the face to fetch required cert does not matter
      // So we use SelfVerifyPolicyManager, and don't call KeyChain.setFace()
      keyChain = new KeyChain(identityManager, new SelfVerifyPolicyManager(identityStorage));
      try {
        identityManager.setDefaultIdentity(new Name(mAppID));
        identityManager.addCertificateAsIdentityDefault(certificate);
        Log.d(TAG, "the default certificate is set to be " + keyChain.getDefaultCertificateName().toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
