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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.accessmanager.database.ScheduleDetail;
import net.named_data.accessmanager.util.Common;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ScheduleCreateDialogFragment extends DialogFragment {
  private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

  public static interface OnScheduleAddRequested {
    public void
    addSchedule(ScheduleDetail scheduleDetail);
  }

  public static ScheduleCreateDialogFragment newInstance() {
    return new ScheduleCreateDialogFragment();
  }

  @NonNull
  @Override
  public Dialog
  onCreateDialog(@Nullable Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.dialog_create_schedule, null);
    ListView listView = (ListView) view.findViewById(R.id.schedule_data_type);
    final DataTypeAdapter dataTypeAdapter = new DataTypeAdapter(getContext(),
      R.layout.data_type_list_item, Common.DATA_TYPES);
    listView.setAdapter(dataTypeAdapter);
    builder
      .setView(view)
      .setPositiveButton(R.string.schedule_add_dialog_create_schedule, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
          try {
            EditText scheduleNameView = (EditText) getDialog().findViewById(R.id.schedule_name);
            String scheduleName = scheduleNameView.getText().toString();
            EditText startDateView = (EditText) getDialog().findViewById(R.id.schedule_start_date);
            String startDate = startDateView.getText().toString();
            df.parse(startDate);
            EditText endDateView = (EditText) getDialog().findViewById(R.id.schedule_end_date);
            String endDate = endDateView.getText().toString();
            df.parse(endDate);
            EditText startHourView = (EditText) getDialog().findViewById(R.id.schedule_start_hour);
            int startHour = Integer.parseInt(startHourView.getText().toString());
            EditText endHourView = (EditText) getDialog().findViewById(R.id.schedule_end_hour);
            int endHour = Integer.parseInt(endHourView.getText().toString());

            if(startHour < 0 || startHour > 23 || endHour < 1 || endHour > 24 || startHour >= endHour
              || startDate.compareTo(endDate) >= 0) {
              Toast.makeText(getContext(), "Please correct the input", Toast.LENGTH_LONG).show();
            } else {
              ScheduleDetail scheduleDetail = new ScheduleDetail(scheduleName,
                Common.DATA_TYPE_PREFIXES[dataTypeAdapter.getselectedPosition()],
                startDate, endDate, startHour, endHour);
              ((OnScheduleAddRequested)getTargetFragment()).addSchedule(scheduleDetail);
            }
          } catch (ParseException e) {
            Toast.makeText(getContext(), "Please correct the input", Toast.LENGTH_LONG).show();
          }
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id)
        {
          ScheduleCreateDialogFragment.this.getDialog().cancel();
        }
      });
    Dialog scheduleCreateDialog = builder.create();
    scheduleCreateDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    return scheduleCreateDialog;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  private static class DataTypeAdapter extends ArrayAdapter<String> {
    private int selectedPosition = 0;
    private final LayoutInflater m_layoutInflater;

    public DataTypeAdapter(Context context, int textViewResourceId, String[] dataTypeArray) {
      super(context, textViewResourceId, dataTypeArray);
      m_layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if(convertView == null) {
        convertView = m_layoutInflater.inflate(R.layout.data_type_list_item, null);
      }
      TextView textView = (TextView) convertView.findViewById(R.id.data_type_name);
      textView.setText(getItem(position));
      RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.data_type_radiobutton);
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
}
