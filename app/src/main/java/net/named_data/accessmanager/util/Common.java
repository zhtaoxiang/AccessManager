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

import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.security.SecurityException;

public abstract class Common {
  public static final int KEY_SIZE = 2048;
  public static final int KEY_FRESHNESS_HOURS = 24 * 365;
  public static final String MANAGER_DB_NAME = "manager.db";
  public static final String DATE_SUFFIX = "T000000";

  // TODO: these two variables should be gotten from id manager
  public static String userPrefix = "/org/openmhealth/haitao";
  public static String accessControlPrefix = userPrefix + "/READ";
  public static KeyChain keyChain = configureKeyChain();


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
}
