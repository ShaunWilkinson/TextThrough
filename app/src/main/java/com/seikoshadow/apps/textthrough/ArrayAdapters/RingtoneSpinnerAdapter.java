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
    private Context context;
    private List<Ringtone> ringtones;
    private LayoutInflater inflater;

    public RingtoneSpinnerAdapter(Context context, List<Ringtone> ringtones) {
        this.context = context;
        this.ringtones = ringtones;
        inflater = (LayoutInflater.from(context));
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.layout_spinner_ringtone, null); //TODO fix this
        TextView title = view.findViewById(R.id.title);
        title.setText(ringtones.get(position).getName());
        return view;
    }
}
