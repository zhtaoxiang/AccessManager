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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.WindowManager;

import net.named_data.accessmanager.database.MembershipDetail;

public class MemberCrateDialogFragment extends DialogFragment {
  public static interface OnMemberCreateRequested {
    public void
    createMember(MembershipDetail membershipDetail);
  }

  public static MemberCrateDialogFragment newInstance() {
    return new MemberCrateDialogFragment();
  }

  @NonNull
  @Override
  public Dialog
  onCreateDialog(@Nullable Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    builder
      .setView(inflater.inflate(R.layout.dialog_create_member, null))
      .setPositiveButton(R.string.member_add_dialog_create_member, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            //TODO: think about how to fill in this part
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
}
