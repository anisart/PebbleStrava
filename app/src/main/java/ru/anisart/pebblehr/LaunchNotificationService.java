package ru.anisart.pebblehr;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.movisens.smartgattlib.Characteristic;
import com.movisens.smartgattlib.characteristics.BatteryLevel;
import com.movisens.smartgattlib.characteristics.HeartRateMeasurement;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * LaunchNotificationService
 * v.1.0
 * 03.04.2017
 * Created by Artyom Anisimov
 * artyom.anisimov@auriga.com
 * Copyright (c) 2017 Auriga Inc. All rights reserved.
 */
public class LaunchNotificationService extends NotificationListenerService {

    private final static String PACK_NAME = "com.strava";
    private RxBleDevice bleDevice;
    private Subscription subscription;

    @Override
    public void onCreate() {
        super.onCreate();
        bleDevice = RxBleClient.create(this).getBleDevice("F9:03:2A:F2:01:20");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        //TODO: News feed and others notification will be captured too
        // return if it is not Strava notification
        if (!packageName.equals(PACK_NAME)) return;

        Notification notification = sbn.getNotification();
        if (subscription == null || subscription.isUnsubscribed()) {
            subscribeNotifications();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        // return if it is not Strava notification
        if (!packageName.equals(PACK_NAME)) return;
        // return if Pebble is not connected
        if (!PebbleKit.isWatchConnected(this)) return;

        System.out.println("REMOVED");

        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void subscribeNotifications() {
        Observable<RxBleConnection> connectionObservable = bleDevice.establishConnection(false);
        subscription = connectionObservable
                .flatMap(connection -> Observable.combineLatest(
                        connection.setupNotification(Characteristic.HEART_RATE_MEASUREMENT)
                                .flatMap(notificationObservable -> notificationObservable),
                        connection.readCharacteristic(Characteristic.BATTERY_LEVEL),
                        this::onNotificationReceived))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::onNotificationSetupSuccess)
                .subscribe(b -> {}, this::onNotificationSetupFailure);
    }

    private boolean onNotificationReceived(byte[] hrBytes, byte[] batBytes) {
        int hrValue = new HeartRateMeasurement(hrBytes).getHr();
        int batteryLevel = new BatteryLevel(batBytes).getBatteryLevel();

        if (!PebbleKit.isWatchConnected(this)) {
            return false;
        }
        PebbleDictionary dict = new PebbleDictionary();
        dict.addInt32(Constants.SPORTS_HR_BPM_KEY, hrValue);;
        dict.addString(Constants.SPORTS_CUSTOM_LABEL_KEY, "BATTERY");
        dict.addString(Constants.SPORTS_CUSTOM_VALUE_KEY, String.format(Locale.US, "%d%%", batteryLevel));
        PebbleKit.sendDataToPebble(getApplicationContext(), Constants.SPORTS_UUID, dict);
        return true;
    }

    private void onNotificationSetupSuccess() {
        Toast.makeText(this, "Notification setup success", Toast.LENGTH_SHORT).show();
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        System.err.println("Notifications error: " + throwable);
    }
}
