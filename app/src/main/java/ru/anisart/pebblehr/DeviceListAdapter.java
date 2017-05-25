package ru.anisart.pebblehr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleDevice;

import java.util.ArrayList;

/**
 * DeviceListAdapter
 * v.1.0
 * 07.12.2016
 * Created by Artyom Anisimov
 * artyom.anisimov@auriga.com
 * Copyright (c) 2016 Auriga Inc. All rights reserved.
 */
public class DeviceListAdapter extends BaseAdapter {

    private final ArrayList<RxBleDevice> mDevices = new ArrayList<>();
    private Context mContext;

    public DeviceListAdapter(Context context) {
        mContext = context;
    }

    public void addDevice(RxBleDevice device) {
        mDevices.add(device);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(android.R.id.text1);
            holder.address = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        }

        final RxBleDevice device = mDevices.get(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.name.setText(device.getName());
        holder.address.setText(device.getMacAddress());

        return convertView;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
    }
}
