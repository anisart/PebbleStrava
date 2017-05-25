package ru.anisart.pebblehr;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.UUID;

/**
 * ScannerFragment
 * v.1.0
 * 07.12.2016
 * Created by Artyom Anisimov
 * artyom.anisimov@auriga.com
 * Copyright (c) 2016 Auriga Inc. All rights reserved.
 */
public class ScannerFragment extends DialogFragment {

    public final static UUID BP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");

    private DeviceListAdapter mAdapter;
    private RxBleClient mRxBleClient;

    public static ScannerFragment getInstance() {
        return new ScannerFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_scannner, null);
        final ListView listview = (ListView) dialogView.findViewById(android.R.id.list);

        listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
        listview.setAdapter(mAdapter = new DeviceListAdapter(getContext()));
        listview.setOnItemClickListener((parent, view, position, id) -> {
            String name = ((RxBleDevice) mAdapter.getItem(position)).getName();
            Toast.makeText(getContext(), name, Toast.LENGTH_SHORT).show();
        });

        builder.setTitle(R.string.scanner_title);
        final AlertDialog dialog = builder.setView(dialogView).create();

        if (savedInstanceState == null) {
            startScan();
        }

        return dialog;
    }

    public void startScan() {
        mRxBleClient = RxBleClient.create(getContext());
        mRxBleClient.scanBleDevices(/*BP_SERVICE_UUID*/)
                .distinct(rxBleScanResult -> rxBleScanResult.getBleDevice().getMacAddress())
                .subscribe(
                        rxBleScanResult -> {
                            System.out.println(rxBleScanResult.getRssi() + " " + rxBleScanResult.getBleDevice().getMacAddress() + " " + rxBleScanResult.getBleDevice().getName());
                            mAdapter.addDevice(rxBleScanResult.getBleDevice());
                        },
                        System.err::println
                );
    }

}
