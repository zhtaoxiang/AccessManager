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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.accessmanager.database.DataBase;
import net.named_data.accessmanager.database.MembershipDetail;
import net.named_data.accessmanager.util.Common;
import net.named_data.accessmanager.util.EntityInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemberCrateDialogFragment extends DialogFragment {
  public static interface OnMemberAddRequested {
    public void
    addMember(MembershipDetail membershipDetail);
  }

  public static MemberCrateDialogFragment newInstance() {
    return new MemberCrateDialogFragment();
  }

  @NonNull
  @Override
  public Dialog
  onCreateDialog(@Nullable final Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.dialog_create_member, null);
    ListView predefinedMemberListView = (ListView) view.findViewById(R.id.member_predefined_members);
    final PredefinedMemberAdapter predefinedMemberTypeAdapter =
      new PredefinedMemberAdapter(getContext(), R.layout.predefined_entity_list_item,
        Common.PREDEFINED_ENTITY_NAMES);
    predefinedMemberListView.setAdapter(predefinedMemberTypeAdapter);
    ListView existingScheduleListView = (ListView) view.findViewById(R.id.member_existing_schedules);
    final String[] allScheduleNames = DataBase.getInstance(getContext()).getAllScheduleNames();
    final ExistingScheduleAdapter existingScheduleAdapter =
      new ExistingScheduleAdapter(getContext(), R.layout.existing_schedule_list_item,
        allScheduleNames);
    existingScheduleListView.setAdapter(existingScheduleAdapter);
    builder
      .setView(view)
      .setPositiveButton(R.string.member_add_dialog_create_member, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            int memberPosition = predefinedMemberTypeAdapter.getselectedPosition();
            Set<Integer> scheduleSet = existingScheduleAdapter.getSelectedPositions();
            if (scheduleSet == null || scheduleSet.isEmpty()) {
              Toast.makeText(getContext(), "Please correct the input", Toast.LENGTH_LONG).show();
            } else {
              String memberName = Common.PREDEFINED_ENTITY_NAMES[memberPosition];
              EntityInfo entityInfo = Common.PREDEFINED_ENTITY_NAME_MAP.get(memberName);
              List<String> scheduleList = new ArrayList<>();
              for(int onePosition : scheduleSet) {
                scheduleList.add(allScheduleNames[onePosition]);
              }
              MembershipDetail membershipDetail =  new MembershipDetail(entityInfo, scheduleList);
              ((OnMemberAddRequested)getTargetFragment()).addMember(membershipDetail);
            }
          }
        })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id)
        {
          MemberCrateDialogFragment.this.getDialog().cancel();
        }
      });
    Dialog scheduleCreateDialog = builder.create();
    scheduleCreateDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    return scheduleCreateDialog;
  }
  /////////////////////////////////////////////////////////////////////////////////////////////
  private static class PredefinedMemberAdapter extends ArrayAdapter<String> {
    private int selectedPosition = 0;
    private final LayoutInflater m_layoutInflater;

    public PredefinedMemberAdapter(Context context, int textViewResourceId, String[] dataTypeArray) {
      super(context, textViewResourceId, dataTypeArray);
      m_layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = m_layoutInflater.inflate(R.layout.predefined_entity_list_item, null);
      }
      TextView textView = (TextView) convertView.findViewById(R.id.predefined_member_name);
      textView.setText(getItem(position));
      RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.predefined_member_radiobutton);
      radioButton.setChecked(position == selectedPosition);
      radioButton.setTag(position);
      radioButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectedPosition = (Integer)view.getTag();
          notifyDataSetChanged();
        }
      });
      return convertView;
    }

    public int getselectedPosition() {
      return selectedPosition;
    }
  }

  private static class ExistingScheduleAdapter extends ArrayAdapter<String> {
    public Set<Integer> selectedPositions = new HashSet<>();
    private final LayoutInflater m_layoutInflater;

    public ExistingScheduleAdapter(Context context, int textViewResourceId, String[] dataTypeArray) {
      super(context, textViewResourceId, dataTypeArray);
      m_layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = m_layoutInflater.inflate(R.layout.existing_schedule_list_item, null);
      }
      TextView textView = (TextView) convertView.findViewById(R.id.existing_schedule_name);
      textView.setText(getItem(position));
      CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.existing_schedule_checkbox);
      if (selectedPositions.contains(position))
        checkBox.setChecked(true);
      else {
        checkBox.setChecked(false);
      }
      checkBox.setTag(position);
      checkBox.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          Integer position = (Integer)view.getTag();
          if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
          } else {
            selectedPositions.add(position);
          }
          notifyDataSetChanged();
        }
      });
      return convertView;
    }

    public Set<Integer> getSelectedPositions() {
      return selectedPositions;
    }
  }
}
