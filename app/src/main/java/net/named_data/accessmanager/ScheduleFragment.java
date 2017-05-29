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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.named_data.accessmanager.database.DataBase;
import net.named_data.accessmanager.database.ScheduleDetail;
import net.named_data.accessmanager.util.Common;

import java.util.List;


public class ScheduleFragment extends ListFragment
  implements ScheduleCreateDialogFragment.OnScheduleAddRequested{
  private ProgressBar m_reloadingListProgressBar;
  private ScheduleListAdapter m_scheduleListAdapter;

  public static ScheduleFragment newInstance() {
    // Create fragment arguments here (if necessary)
    return new ScheduleFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_schedule_list_header, null);
    getListView().addHeaderView(v, null, false);
    getListView().setDivider(getResources().getDrawable(R.drawable.list_item_divider));

    // Get progress bar spinner view
    m_reloadingListProgressBar
      = (ProgressBar)v.findViewById(R.id.schedule_list_reloading_list_progress_bar);
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);

    if (m_scheduleListAdapter == null) {
      m_scheduleListAdapter = new ScheduleListAdapter(getActivity());
    }
    // setListAdapter must be called after addHeaderView.  Otherwise, there is an exception on some platforms.
    // http://stackoverflow.com/a/8141537/2150331
    setListAdapter(m_scheduleListAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    retrieveScheduleList();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_schedule_list, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId()) {
      case R.id.schedule_list_refresh:
        retrieveScheduleList();
        return true;
      case R.id.schedule_list_add:
        ScheduleCreateDialogFragment dialog = ScheduleCreateDialogFragment.newInstance();
        dialog.setTargetFragment(ScheduleFragment.this, 0);
        dialog.show(getFragmentManager(), "ScheduleCreateDialogFragment");
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onDestroyView()
  {
    super.onDestroyView();
    setListAdapter(null);
  }

  @Override
  public void addSchedule(ScheduleDetail scheduleDetail) {
    try {
      ((MainActivity)getActivity()).mService.addOneSchedule(scheduleDetail, true);
      DataBase.getInstance(getContext()).insertSchedule(scheduleDetail);
    } catch (Exception e) {
      e.printStackTrace();
    }
    retrieveScheduleList();
  }

  /////////////////////////////////////////////////////////////////////////////////////
  private static class ScheduleListAdapter extends BaseAdapter {
    private final LayoutInflater m_layoutInflater;
    private List<ScheduleDetail> m_scheduleDetailList;

    private ScheduleListAdapter(Context context) {
      m_layoutInflater = LayoutInflater.from(context);
    }

    private void
    updateList(List<ScheduleDetail> scheduleDetailList) {
      m_scheduleDetailList = scheduleDetailList;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return (m_scheduleDetailList != null) ? m_scheduleDetailList.size() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ScheduleInfoHolder holder;
      if(convertView == null) {
        holder = new ScheduleInfoHolder();
        convertView = m_layoutInflater.inflate(R.layout.schedule_list_item, null);
        convertView.setTag(holder);
        holder.m_scheduleName = (TextView)convertView.findViewById(R.id.list_item_name);
        holder.m_scheduleDataType = (TextView)convertView.findViewById(R.id.list_item_datatype);
        holder.m_scheduleDate = (TextView)convertView.findViewById(R.id.list_item_date);
        holder.m_scheduleHour = (TextView)convertView.findViewById(R.id.list_item_hour);
      } else {
        holder = (ScheduleInfoHolder)convertView.getTag();
      }
      ScheduleDetail info = getItem(position);
      holder.m_scheduleName.setText(info.getName());
      holder.m_scheduleDataType.setText("Prefix: " + Common.accessControlPrefix + info.getPrefix());
      holder.m_scheduleDate.setText("Date: " + info.getStartDate() + " - " + info.getEndDate());
      holder.m_scheduleHour.setText("Hour: " + info.getStartHour() + " - " + info.getEndHour());
      return convertView;
    }

    @Override
    public long getItemId(int i)
    {
      return i;
    }

    @Override
    public ScheduleDetail
    getItem(int i)
    {
      assert m_scheduleDetailList != null;
      return m_scheduleDetailList.get(i);
    }

    private static class ScheduleInfoHolder {
      private TextView m_scheduleName;
      private TextView m_scheduleDataType;
      private TextView m_scheduleDate;
      private TextView m_scheduleHour;
    }
  }

  private void retrieveScheduleList() {
    List<ScheduleDetail> scheduleDetailList = DataBase.getInstance(getContext()).getAllSchedules();
    ((ScheduleListAdapter)getListAdapter()).updateList(scheduleDetailList);
  }
}
