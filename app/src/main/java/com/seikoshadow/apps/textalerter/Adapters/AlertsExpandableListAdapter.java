package com.seikoshadow.apps.textalerter.Adapters;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.seikoshadow.apps.textalerter.Database.Alert;
import com.seikoshadow.apps.textalerter.Database.AlertModel;
import com.seikoshadow.apps.textalerter.Database.AppDatabase;
import com.seikoshadow.apps.textalerter.Dialogs.EditAlertDialogFragment;
import com.seikoshadow.apps.textalerter.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class AlertsExpandableListAdapter extends BaseExpandableListAdapter {
    private final String TAG = "AlertsExpandableListAdapter";
    private Context _context;
    private List<Alert> alerts;

    public AlertsExpandableListAdapter (Context context, List<Alert> alerts) {
        this._context = context;
        this.alerts = alerts;
    }

    public void addItems(List<Alert> alerts) {
        this.alerts.clear();
        this.alerts = alerts;
        notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition);
        View v = convertView;

        if(v == null) {
            LayoutInflater inflater = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(inflater != null)
                v = inflater.inflate(R.layout.expandable_list_parent, parent, false);
            else
                return null;
        }

        TextView itemTitleText = v.findViewById(R.id.alertsItemHeading);

        itemTitleText.setText(headerTitle);

        // Change the view background depending on whether it is expanded or not
        if(isExpanded) {
            //v.setBackgroundColor(ContextCompat.getColor(_context, R.color.colorPrimaryLightDark));
            v.setBackground(ContextCompat.getDrawable(_context, R.drawable.expandable_list_parent_background));
            itemTitleText.setTextColor(ContextCompat.getColor(_context, R.color.colorWhite));
        } else {
            v.setBackgroundColor(ContextCompat.getColor(_context, R.color.colorPrimary));
            itemTitleText.setTextColor(ContextCompat.getColor(_context, R.color.colorGreyText));
        }

        return v;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(layoutInflater != null)
                v = layoutInflater.inflate(R.layout.expandable_list_child, parent, false);
            else {
                return null;
            }
        }

        // Convert all labels to lower case
        List<TextView> labels = new ArrayList<>();
        labels.add(v.findViewById(R.id.alertsListChildPhoneLabel));
        labels.add(v.findViewById(R.id.alertsListChildRingtoneLabel));
        labels.add(v.findViewById(R.id.alertsListChildActiveLabel));
        labels.add(v.findViewById(R.id.alertsListChildRingLabel));
        labels.add(v.findViewById(R.id.alertsListChildVibrateLabel));
        for(TextView label : labels) {
            label.setText(label.getText().toString().toLowerCase());
        }

        // initiate the fields
        TextView phoneNumberText = v.findViewById(R.id.alertsListChildPhoneText);
        TextView ringtoneNameText = v.findViewById(R.id.alertsListChildRingtoneText);
        TextView alertActiveText = v.findViewById(R.id.alertsListChildActiveText);
        TextView ringCountText = v.findViewById(R.id.alertsListChildRingText);
        TextView vibrateText = v.findViewById(R.id.alertsListChildVibrateText);

        // If true set to yes, otherwise No
        String alertActive = alerts.get(groupPosition).isAlertActive() ? _context.getString(R.string.yes) : _context.getString(R.string.no);
        String vibrate = alerts.get(groupPosition).isAlertVibrate() ? _context.getString(R.string.yes) : _context.getString(R.string.no);
        String ringCount = String.valueOf(alerts.get(groupPosition).getSecondsToRingFor());

        // Set the values
        phoneNumberText.setText(alerts.get(groupPosition).getPhoneNumber());
        ringtoneNameText.setText(alerts.get(groupPosition).getRingtoneName());
        alertActiveText.setText(alertActive);
        ringCountText.setText(ringCount);
        vibrateText.setText(vibrate);

        // Set up the edit button
        Button editBtn = v.findViewById(R.id.editBtn);
        editBtn.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putLong("Alert Id", alerts.get(groupPosition).getId());

            EditAlertDialogFragment dialog = new EditAlertDialogFragment();
            dialog.setArguments(bundle);

            FragmentTransaction fragmentTransaction = ((Activity) _context).getFragmentManager().beginTransaction();
            dialog.show(fragmentTransaction, EditAlertDialogFragment.TAG);
        });

        // Set up the delete button
        Button deleteBtn = v.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(_context)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this alert?")
                .setPositiveButton(android.R.string.yes, (confirmDialog, which) -> {
                    // Run the delete in the background
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        // Make sure theres not an existing record with the same phone number
                        AlertModel alertModel = AppDatabase.getInstance(_context.getApplicationContext()).alertModel();
                        alertModel.delete(alerts.get(groupPosition));
                    });

                    confirmDialog.dismiss();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
        });

        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getGroupCount() {
        return this.alerts.size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return alerts.get(groupPosition).getName();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return alerts.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public Alert getChild(int groupPosition, int childPosition) {
        return null;
    }

}
