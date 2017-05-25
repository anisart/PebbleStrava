package ru.anisart.pebblehr;

import android.Manifest;
import android.app.Notification;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.characteristics.BatteryLevel;
import com.movisens.smartgattlib.characteristics.HeartRateMeasurement;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.serviceButton)
    ToggleButton serviceButton;
    @BindView(R.id.deviceName)
    TextView deviceNameView;
    @BindView(R.id.scanButton)
    Button scanButton;
    @BindView(R.id.heartValue)
    TextView heartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        serviceButton.setOnClickListener(
                v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        scanButton.setOnClickListener(
                v -> MainActivityPermissionsDispatcher.scanDevicesWithCheck(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (NotificationManagerCompat.getEnabledListenerPackages(this).contains(getApplication().getPackageName())) {
            serviceButton.setChecked(true);
            serviceButton.setText("ON");
        } else {
            serviceButton.setChecked(false);
            serviceButton.setText("OFF");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    void scanDevices() {
        final ScannerFragment dialog = ScannerFragment.getInstance();
        dialog.show(getSupportFragmentManager(), "scan_fragment");
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    void onLocationDenied() {

    }
}
