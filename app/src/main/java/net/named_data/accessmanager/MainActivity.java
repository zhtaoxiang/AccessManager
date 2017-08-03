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
package net.named_data.accessmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.named_data.accessmanager.database.DataBase;
import net.named_data.accessmanager.service.AccessManagerService;
import net.named_data.accessmanager.util.Common;
import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.Sha256WithRsaSignature;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.identity.AndroidSqlite3IdentityStorage;
import net.named_data.jndn.security.identity.FilePrivateKeyStorage;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.PrivateKeyStorage;
import net.named_data.jndn.util.Blob;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
  implements DrawerFragment.DrawerCallbacks {

  @Override
  protected void onStart() {
    super.onStart();
    // Bind to LocalService
    Intent intent = new Intent(this, AccessManagerService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    // Unbind from the service
    if (mBound) {
      unbindService(mConnection);
      mBound = false;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FragmentManager fragmentManager = getSupportFragmentManager();

    if (savedInstanceState != null) {
      m_drawerFragment = (DrawerFragment) fragmentManager.findFragmentByTag(DrawerFragment.class.toString());
    }

    if (m_drawerFragment == null) {
      ArrayList<DrawerFragment.DrawerItem> items = new ArrayList<>();

      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_schedule_list, 0,
        DRAWER_ITEM_SCHEDULE_LIST));

      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_member_list, 0,
        DRAWER_ITEM_MEMBER_LIST));

      m_drawerFragment = DrawerFragment.newInstance(items);

      fragmentManager
        .beginTransaction()
        .replace(R.id.navigation_drawer, m_drawerFragment, DrawerFragment.class.toString())
        .commit();
    }

    Common.mCtx = getApplicationContext();

    DataBase db = DataBase.getInstance(getApplicationContext());
    Cursor idRecords = db.getIdRecord();
    if (idRecords.moveToNext()) {
      String mAppId = idRecords.getString(0);
      Name mAppCertificateName = new Name(idRecords.getString(1));

      Common.setUserPrefix(new Name(mAppId).getPrefix(-1));
      Common.setAppID(mAppId, mAppCertificateName);
      // omit the app name component from mAppId
      idRecords.close();
      Log.d(TAG, "try to start service");
      startService(new Intent(this, AccessManagerService.class));
    } else {
      requestAuthorization();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!m_drawerFragment.shouldHideOptionsMenu()) {
      updateActionBar();
      return super.onCreateOptionsMenu(menu);
    }
    else
      return true;
  }

  private void updateActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    if (m_actionBarTitleId != -1) {
      actionBar.setTitle(m_actionBarTitleId);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  /**
   * Convenience method that replaces the main fragment container with the
   * new fragment and adding the current transaction to the backstack.
   *
   * @param fragment Fragment to be displayed in the main fragment container.
   */
  private void replaceContentFragmentWithBackstack(Fragment fragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
      .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
      .replace(R.id.main_fragment_container, fragment)
      .addToBackStack(null)
      .commit();
  }

  @Override
  public void
  onDrawerItemSelected(int itemCode, int itemNameId) {

    String fragmentTag = "net.named-data.accessmanager.content-" + String.valueOf(itemCode);
    FragmentManager fragmentManager = getSupportFragmentManager();

    // Create fragment according to user's selection
    Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
    if (fragment == null) {
      switch (itemCode) {
        case DRAWER_ITEM_SCHEDULE_LIST:
          fragment = ScheduleFragment.newInstance();
          break;
        case DRAWER_ITEM_MEMBER_LIST:
          fragment = MemberFragment.newInstance();
          break;
        default:
          return;
      }
    }

    // Update ActionBar title
    m_actionBarTitleId = itemNameId;

    fragmentManager.beginTransaction()
      .replace(R.id.main_fragment_container, fragment, fragmentTag)
      .commit();
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      AccessManagerService.LocalBinder binder = (AccessManagerService.LocalBinder) service;
      mService = binder.getServiceInstance();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  // @param certName This string is intended to be the application's id in the future, left as "stub" for now
  public void requestAuthorization() {
    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
    dlgAlert.setMessage("Please choose an identity!");
    dlgAlert.setTitle("Choose an identity");
    dlgAlert.setPositiveButton("Ok", new AuthorizeOnClickListener(APP_NAME));
    dlgAlert.setCancelable(true);
    dlgAlert.create().show();
  }

  public class AuthorizeOnClickListener implements DialogInterface.OnClickListener {
    String mAppName;

    public AuthorizeOnClickListener(String appName) {
      mAppName = appName;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
      Intent i = new Intent("com.ndn.jwtan.identitymanager.AUTHORIZE");
      i.putExtra("app_id", mAppName);
      startActivityForResult(i, AUTHORIZE_REQUEST);
    }
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Check which request we're responding to
    if (requestCode == AUTHORIZE_REQUEST) {
      // Make sure the request was successful
      if (resultCode == Activity.RESULT_OK) {
        String signerID = data.getStringExtra("prefix");
        Name appID = new Name(signerID).append(APP_NAME);
        mAppId = appID.toUri();
        try {
          String encodedString = generateKey(appID.toString());
          requestSignature(encodedString, signerID);
        } catch (Exception e) {
          Log.e(TAG, "Exception in identity generation/request");
          Log.e(TAG, e.getMessage());
        }
      }
    } else if (requestCode == SIGN_CERT_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
        String signedCert = data.getStringExtra("signed_cert");
        byte[] decoded = Base64.decode(signedCert, Base64.DEFAULT);
        Blob blob = new Blob(decoded);
        Data certData = new Data();
        try {
          if (!mAppId.isEmpty()) {
            certData.wireDecode(blob);
            IdentityCertificate certificate = new IdentityCertificate(certData);
            String signerKey = ((Sha256WithRsaSignature) certificate.getSignature()).getKeyLocator().getKeyName().toUri();
            Log.d(TAG, "Signer key name " + signerKey);
            Log.d(TAG,"App certificate name: " + certificate.getName().toUri());
            DataBase.getInstance(getApplicationContext()).insertID(mAppId, certificate.getName().toUri(), signerKey);
            mAppCertificateName = new Name(certificate.getName());
            Common.setUserPrefix(new Name(mAppId).getPrefix(-1));
            Common.setAppID(mAppId, certificate);
            Log.d(TAG, "try to start service");
            startService(new Intent(this, AccessManagerService.class));
          } else {
            Log.e(TAG, "mAppId empty for result of SIGN_CERT_REQUEST");
          }
        } catch (Exception e) {
          Log.e(getResources().getString(R.string.app_name), e.getMessage());
        }
      }
    }
  }



  private String generateKey(String appID) throws net.named_data.jndn.security.SecurityException {
    String dbPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.DB_NAME;
    String certDirPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + MainActivity.CERT_DIR;

    IdentityStorage identityStorage = new AndroidSqlite3IdentityStorage(dbPath);
    PrivateKeyStorage privateKeyStorage = new FilePrivateKeyStorage(certDirPath);
    IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);

    Name identityName = new Name(appID);

    Name keyName = identityManager.generateRSAKeyPairAsDefault(identityName, true);
    IdentityCertificate certificate = identityManager.selfSign(keyName);

    String encodedString = Base64.encodeToString(certificate.wireEncode().getImmutableArray(), Base64.DEFAULT);
    return encodedString;
  }

  private void requestSignature(String encodedString, String signerID) {
    Intent i = new Intent("com.ndn.jwtan.identitymanager.SIGN_CERTIFICATE");
    i.putExtra("cert", encodedString);
    i.putExtra("signer_id", signerID);
    i.putExtra("app_id", APP_NAME);
    startActivityForResult(i, SIGN_CERT_REQUEST);
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  private static final String APP_NAME = "access_manager";
  private static final int AUTHORIZE_REQUEST = 1003;
  private static final int SIGN_CERT_REQUEST = 1004;
  String mAppId;
  Name mAppCertificateName;

  // Storage for app keys
  public static final String DB_NAME = "certDb.db";
  public static final String CERT_DIR = "certDir";

  //////////////////////////////////////////////////////////////////////////////
  private static final String TAG = "MainActivity";

  AccessManagerService mService;
  boolean mBound = false;

  /** Title that is to be displayed in the ActionBar */
  private int m_actionBarTitleId = -1;

  /** Reference to drawer fragment */
  private DrawerFragment m_drawerFragment;

  /** Item code for drawer items: For use in onDrawerItemSelected() callback */
  public static final int DRAWER_ITEM_SCHEDULE_LIST = 1;
  public static final int DRAWER_ITEM_MEMBER_LIST = 2;
}
