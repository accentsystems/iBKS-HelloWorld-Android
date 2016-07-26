package com.accent_systems.ibkshelloworld;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BackgroundScan extends Application implements BootstrapNotifier, BeaconConsumer {
    private static final String TAG = "BackgroundScan";
    private RegionBootstrap regionBootstrap;
    private BeaconManager mBeaconManager;

    Region regions[];

    public void onCreate() {


        super.onCreate();
        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();

        //set Beacon Layout for iBeacon packet
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        //set Beacon Layout for Eddystone-UID packet
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        //set period of scan in background and foreground. In every period 'didRangeBeaconsInRegion' callback is called
        mBeaconManager.setForegroundScanPeriod(5100);
        mBeaconManager.setForegroundBetweenScanPeriod(3000);
        mBeaconManager.setBackgroundScanPeriod(5100);
        mBeaconManager.setBackgroundBetweenScanPeriod(6000);
        BeaconManager.setAndroidLScanningDisabled(true);


        regions = new Region[2];
        //With "new Region" you are adding the beacon identifier to the list that will be checked in every Scan Period
        //To add iBeacon region it's necessary to pass as parameters --> (uniqueId = region name, id1=uuid, id2 = major, id3 = minor)
        regions[0] = new Region("iBeaconAdvertising", Identifier.parse("00000000000000000000000000000000"), Identifier.parse(""+Integer.parseInt("0006", 16)), Identifier.parse(""+Integer.parseInt("0006", 16)));
        //To add Eddystone-UID region it's necessary to pass as parameters --> (uniqueId = region name, id1=namespace, id2 = instance, id3 = null)
        regions[1] = new Region("EdstUIDAdvertising", Identifier.parse("0x00000000000000000000"), Identifier.parse("0x000000000001"), null);


        mBeaconManager.bind(this);

    }



    public void enableRegions() {
        try {

            if (regionBootstrap == null) {
                List<Region> list = new ArrayList<>();
                for (int i = 0; i < regions.length; i++) {
                    if (regions[i] != null) {
                        list.add(regions[i]);
                    }
                }
                regionBootstrap = new RegionBootstrap(this, list);
            }

            for (int i = 0; i < regions.length; i++) {
                if (regions[i] != null) {
                    mBeaconManager.startRangingBeaconsInRegion(regions[i]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableRegions() {
        try {

            if (regionBootstrap != null)
                regionBootstrap.disable();
            regionBootstrap = null;

            for (int i = 0; i < regions.length; i++) {
                if (regions[i] != null) {
                    mBeaconManager.stopRangingBeaconsInRegion(regions[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {


        mBeaconManager.setRangeNotifier(new RangeNotifier() {

            @Override
            //Here enters each scan period
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                Log.i(TAG, "Found " + beacons.size() + " beacons in Region "+ region.getUniqueId() + " - " + region.getId1()+ " - " + region.getId2()+ " - " + region.getId3());

                //if number of beacons in region is greater than '0' it means that a beacon or beacons with the correspondent identifier is/are detected.
                if(beacons.size() > 0) {
                    for (Beacon beacon: beacons) {
                        int rssi = beacon.getRssi();
                        //Do something when a packet is received (notification, dialog, open app, ...)
                        /*** EXAMPLE CODE FOR NOTIFICATION ***
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(BackgroundScan.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("¡Beacon found!")
                                .setContentText("You found this beacon ID: "+  region.getId1()+ " - " + region.getId2()+ " - " + region.getId3() + " with RSSI = "+ Integer.toString(rssi))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("You found this beacon ID: "+  region.getId1()+ " - " + region.getId2()+ " - " + region.getId3()+ " with RSSI = "+ Integer.toString(rssi)));

                            builder.setAutoCancel(true);
                            builder.setLights(Color.BLUE, 500, 500);

                            Notification notification = builder.build();

                            NotificationManager mNotificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(1111, notification); // number that identifies our notification

                        /*************************************/
                    }
                }
            }
        });
        //add regions to the list and start scan
        enableRegions();
    }

    @Override
    public void didEnterRegion(Region region) {

        Log.d(TAG, "Beacon detected with namespace id " + region.getId1() +" and instance id: " + region.getId2());
    }

    @Override
    public void didExitRegion(Region region) {

        Log.d(TAG, "Beacon out of region with namespace id " + region.getId1() +" and instance id: " + region.getId2());
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {
        //Ignore
    }
}


