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
import net.named_data.accessmanager.database.MembershipDetail;

import java.util.Arrays;
import java.util.List;

public class MemberFragment extends ListFragment
  implements MemberCrateDialogFragment.OnMemberAddRequested{
  private ProgressBar m_reloadingListProgressBar;
  private MemberListAdapter m_memberListAdapter;

  public static MemberFragment newInstance() {
    return new MemberFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_member_list_header, null);
    getListView().addHeaderView(v, null, false);
    getListView().setDivider(getResources().getDrawable(R.drawable.list_item_divider));

    // Get progress bar spinner view
    m_reloadingListProgressBar
      = (ProgressBar)v.findViewById(R.id.member_list_reloading_list_progress_bar);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if(m_memberListAdapter == null) {
      m_memberListAdapter = new MemberListAdapter(getActivity());
    }
    // setListAdapter must be called after addHeaderView.  Otherwise, there is an exception on some platforms.
    // http://stackoverflow.com/a/8141537/2150331
    setListAdapter(m_memberListAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    retrieveMemberList();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_member_list, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.member_list_refresh:
        retrieveMemberList();
        return true;
      case R.id.member_list_add:
        MemberCrateDialogFragment dialog = MemberCrateDialogFragment.newInstance();
        dialog.setTargetFragment(MemberFragment.this, 0);
        dialog.show(getFragmentManager(), "MemberCrateDialogFragment");
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
  public void addMember(MembershipDetail membershipDetail) {
    try {
      ((MainActivity)getActivity()).mService.addOneMember(membershipDetail);
      DataBase.getInstance(getContext()).insertMember(membershipDetail);
    } catch (Exception e) {
      e.printStackTrace();
    }
    retrieveMemberList();
  }


  /////////////////////////////////////////////////////////////////////////////////////
  private static class MemberListAdapter extends BaseAdapter {
    private final LayoutInflater m_layoutInflater;
    private List<MembershipDetail> m_membershipDetailList;

    private MemberListAdapter(Context context) {
      m_layoutInflater = LayoutInflater.from(context);
    }

    private void
    updateList(List<MembershipDetail> membershipDetailList) {
      m_membershipDetailList = membershipDetailList;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return (m_membershipDetailList != null) ? m_membershipDetailList.size() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      MemberInfoHolder holder;
      if(convertView == null) {
        holder = new MemberInfoHolder();
        convertView = m_layoutInflater.inflate(R.layout.member_list_item, null);
        convertView.setTag(holder);
        holder.m_memberName = (TextView)convertView.findViewById(R.id.list_item_name);
        holder.m_memberId = (TextView)convertView.findViewById(R.id.list_item_id);
        holder.m_memberKey = (TextView)convertView.findViewById(R.id.list_item_key);
        holder.m_scheduleList = (TextView)convertView.findViewById(R.id.list_item_schedule_list);
      } else {
        holder = (MemberInfoHolder) convertView.getTag();
      }
      MembershipDetail info = getItem(position);
      holder.m_memberName.setText("Member Name: " + info.getName());
      holder.m_memberId.setText("Member ID: " + info.getId());
      holder.m_memberKey.setText("Member Cert: " + info.getCert());
      String[] scheduleArray = new String[info.getScheduleList().size()];
      info.getScheduleList().toArray(scheduleArray);
      holder.m_scheduleList.setText("Schedule List: " + Arrays.toString(scheduleArray));
      return convertView;
    }

    @Override
    public long getItemId(int i)
    {
      return i;
    }

    @Override
    public MembershipDetail
    getItem(int i)
    {
      assert m_membershipDetailList != null;
      return m_membershipDetailList.get(i);
    }

    private static class MemberInfoHolder {
      private TextView m_memberName;
      private TextView m_memberId;
      private TextView m_memberKey;
      private TextView m_scheduleList;
    }
  }

  private void retrieveMemberList() {
    List<MembershipDetail> membershipDetailList = DataBase.getInstance(getContext()).getAllMembers();
    ((MemberListAdapter)getListAdapter()).updateList(membershipDetailList);
  }
}
