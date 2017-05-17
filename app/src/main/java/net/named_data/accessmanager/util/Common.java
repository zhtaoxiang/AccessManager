package net.named_data.accessmanager.util;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.security.SecurityException;

/**
 * Created by zhtaoxiang on 5/17/17.
 */

public abstract class Common {
  public static final int KEY_SIZE = 2048;
  public static final int KEY_FRESHNESS_HOURS = 24 * 365;
  public static final String MANAGE_DB_NAME = "manager.db";

  // TODO: these two variables should be gotten from id manager
  public static String userPrefix = "/org/openmhealth/haitao";
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
