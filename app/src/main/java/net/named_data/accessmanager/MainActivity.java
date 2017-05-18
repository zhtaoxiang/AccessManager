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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.named_data.accessmanager.service.AccessManagerService;

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

  //////////////////////////////////////////////////////////////////////////////
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
