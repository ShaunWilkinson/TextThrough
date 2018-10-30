package com.seikoshadow.apps.textthrough.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.seikoshadow.apps.textthrough.Database.Alert;
import com.seikoshadow.apps.textthrough.R;

import java.util.List;

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
        Log.e(TAG, alerts.toString());
        notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition);
        View v = convertView;

        if(v == null) {
            LayoutInflater inflater = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.alerts_listview_item, parent, false);
        }

        TextView itemTitleText = v.findViewById(R.id.alertsItemHeading);

        itemTitleText.setText(headerTitle);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(layoutInflater != null)
                v = layoutInflater.inflate(R.layout.alerts_listview_child, parent, false);
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
        String ringCount = String.valueOf(alerts.get(groupPosition).getNumberOfRings());

        // Set the values
        phoneNumberText.setText(alerts.get(groupPosition).getPhoneNumber());
        ringtoneNameText.setText(alerts.get(groupPosition).getRingtoneName());
        alertActiveText.setText(alertActive);
        ringCountText.setText(ringCount);
        vibrateText.setText(vibrate);

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
        //TODO
        return null;
    }

}
