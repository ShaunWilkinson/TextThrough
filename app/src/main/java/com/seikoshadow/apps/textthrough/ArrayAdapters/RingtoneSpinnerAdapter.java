package com.seikoshadow.apps.textthrough.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.seikoshadow.apps.textthrough.Entities.Ringtone;
import com.seikoshadow.apps.textthrough.R;

import java.util.List;

public class RingtoneSpinnerAdapter extends BaseAdapter {
    private List<Ringtone> ringtones;
    private LayoutInflater inflater;

    public RingtoneSpinnerAdapter(Context currentContext, List<Ringtone> ringtones) {
        this.ringtones = ringtones;
        inflater = (LayoutInflater.from(currentContext));
    }

    @Override
    public int getCount() {
        return ringtones.size();
    }

    @Override
    public Ringtone getItem(int i) {
        return ringtones.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        // For the first item 'view' will be null, populate the ViewHolder. For every other view get references
        // from the ViewHolder
        if(view == null) {
            view = inflater.inflate(R.layout.layout_spinner_ringtone, null); //TODO fix this

            viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.title);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.title.setText(ringtones.get(position).getName());

        return view;
    }


    static class ViewHolder {
        TextView title;
    }
}
