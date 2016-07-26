package com.accent_systems.ibkshelloworld;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    //DEFINE VARS
    String TAG = "ScanActivity";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothGatt mBluetoothGatt;
    BluetoothLeScanner scanner;
    ScanSettings scanSettings;


    private List<String> scannedDeivcesList;
    private ArrayAdapter<String> adapter;

    //DEFINE LAYOUT
    ListView devicesList;

    //THIS METHOD RUNS ON APP LAUNCH
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //Define listview in layout
        devicesList = (ListView) findViewById(R.id.devicesList);
        //Setup list on device click listener
        setupListClickListener();

        //Inicialize de devices list
        scannedDeivcesList = new ArrayList<>();

        //Inicialize the list adapter for the listview with params: Context / Layout file / TextView ID in layout file / Devices list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, scannedDeivcesList);

        //Set the adapter to the listview
        devicesList.setAdapter(adapter);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SpannableString s = new SpannableString("Scan beacons");

        s.setSpan(new com.accent_systems.ibkshelloworld.TypefaceSpan(this, "Khand-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#3a3c3e")), 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setTitle(s);

        getSupportActionBar().setLogo(R.mipmap.ibks);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //init Bluetooth adapter
        initBT();
        //Start scan of bluetooth devices
        startLeScan(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        startLeScan(false);
    }

    private void initBT(){
        final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Create the scan settings
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        //Set scan latency mode. Lower latency, faster device detection/more battery and resources consumption
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //Wrap settings together and save on a settings var (declared globally).
        scanSettings = scanSettingsBuilder.build();
        //Get the BLE scanner from the BT adapter (var declared globally)
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startLeScan(boolean endis) {
        if (endis) {
            //********************
            //START THE BLE SCAN
            //********************
            //Scanning parameters FILTER / SETTINGS / RESULT CALLBACK. Filter are used to define a particular
            //device to scan for. The Callback is defined above as a method.
            scanner.startScan(null, scanSettings, mScanCallback);
        }else{
            //Stop scan
            scanner.stopScan(mScanCallback);
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Here will be received all the detected BLE devices around. "result" contains the device
            //address and name as a BLEPeripheral, the advertising content as a ScanRecord, the Rx RSSI
            //and the timestamp when received. Type result.get... to see all the available methods you can call.

            //Convert advertising bytes to string for a easier parsing. GetBytes may return a NullPointerException. Treat it right(try/catch).
            String advertisingString = byteArrayToHex(result.getScanRecord().getBytes());
            //Print the advertising String in the LOG with other device info (ADDRESS - RSSI - ADVERTISING - NAME)
            Log.i(TAG, result.getDevice().getAddress()+" - RSSI: "+result.getRssi()+"\t - "+advertisingString+" - "+result.getDevice().getName());

            //Check if scanned device is already in the list by mac address
            boolean contains = false;
            for(int i=0; i<scannedDeivcesList.size(); i++){
                if(scannedDeivcesList.get(i).contains(result.getDevice().getAddress())){
                    //Device already added
                    contains = true;
                    //Replace the device with updated values in that position
                    scannedDeivcesList.set(i, result.getRssi()+"  "+result.getDevice().getName()+ "\n       ("+result.getDevice().getAddress()+")");
                    break;
                }
            }

            if(!contains){
                //Scanned device not found in the list. NEW => add to list
                scannedDeivcesList.add(result.getRssi()+"  "+result.getDevice().getName()+ "\n       ("+result.getDevice().getAddress()+")");
            }

            //After modify the list, notify the adapter that changes have been made so it updates the UI.
            //UI changes must be done in the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    };

    //Method to convert a byte array to a HEX. string.
    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    void setupListClickListener(){
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Stop the scan
                Log.i(TAG, "SCAN STOPED");
                scanner.stopScan(mScanCallback);

                //Get the string from the item clicked
                String fullString = scannedDeivcesList.get(position);
                //Get only the address from the previous string. Substring from '(' to ')'
                String address = fullString.substring(fullString.indexOf("(")+1, fullString.indexOf(")"));
                //Get BLE device with address
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                //******************************
                //START CONNECTION WITH DEVICE AND DECLARE GATT
                //******************************
                Log.i(TAG,"*************************************************");
                Log.i(TAG, "CONNECTION STARTED TO DEVICE "+address);
                Log.i(TAG,"*************************************************");

                //ConnectGatt parameters are CONTEXT / AUTOCONNECT to connect the next time it is scanned / GATT CALLBACK to receive GATT notifications and data
                // Note: On Samsung devices, the connection must be done on main thread
                mBluetoothGatt = device.connectGatt(ScanActivity.this, false, mGattCallback);

                /*
                There is also another simplest way to connect to a device. If you already stored
                the device in a list (List<BluetoothDevice>) you can retrieve it directly and
                connect to it:

                mBluetoothGatt = mList.get(position).connectGatt(MainActivity.this, false, mGattCallback);
                 */
            }
        });
    }

    //Connection callback
    BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //Device connected, start discovering services
                        Log.i(TAG, "DEVICE CONNECTED. DISCOVERING SERVICES...");
                        mBluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //Device disconnected
                        Log.i(TAG, "DEVICE DISCONNECTED");
                    }
                }

                // On discover services method
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Services discovered successfully. Start parsing services and characteristics
                        Log.i(TAG, "SERVICES DISCOVERED. PARSING...");
                        displayGattServices(gatt.getServices());
                    } else {
                        //Failed to discover services
                        Log.i(TAG, "FAILED TO DISCOVER SERVICES");
                    }
                }

                //When reading a characteristic, here you receive the task result and the value
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //READ WAS SUCCESSFUL
                        Log.i(TAG, "ON CHARACTERISTIC READ SUCCESSFUL");
                        //Read characteristic value like:
                        //characteristic.getValue();
                        //Which it returns a byte array. Convert it to HEX. string.
                    } else {
                        Log.i(TAG, "ERROR READING CHARACTERISTIC");
                    }
                }

                //When writing, here you can check whether the task was completed successfully or not
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "ON CHARACTERISTIC WRITE SUCCESSFUL");
                    } else {
                        Log.i(TAG, "ERROR WRITING CHARACTERISTIC");
                    }
                }

                //In this method you can read the new values from a received notification
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.i(TAG, "NEW NOTIFICATION RECEIVED");
                    //New notification received. Check the characteristic it comes from and parse to string
                    /*if(characteristic.getUuid().toString().contains("0000fff3")){
                        characteristic.getValue();
                    }*/
                }

                //RSSI values from the connection with the remote device are received here
                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    Log.i(TAG, "NEW RSSI VALUE RECEIVED");
                    //Read remote RSSI like: mBluetoothGatt.readRemoteRssi();
                    //Here you get the gatt table where the rssi comes from, the rssi value and the
                    //status of the task.
                }
            };

    //Method which parses all services and characteristics from the GATT table.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        //Check if there is any gatt services. If not, return.
        if (gattServices == null) return;

        // Loop through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            Log.i(TAG, "SERVICE FOUND: "+gattService.getUuid().toString());
            //Loop through available characteristics for each service
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                Log.i(TAG, "  CHAR. FOUND: "+gattCharacteristic.getUuid().toString());
            }
        }

        //****************************************
        // CONNECTION PROCESS FINISHED!
        //****************************************
        Log.i(TAG, "*************************************");
        Log.i(TAG, "CONNECTION COMPLETED SUCCESFULLY");
        Log.i(TAG, "*************************************");

    }
}


/*

        One you have connected and discovered all services, you can start reading, writing and
        enabling notifications for the characteristics.

        First save on a var the characteristic you want in the loop above as:

        *************************************************************************
        * if(gattCharacteristic.getUuid().toString().contains("0000fff3")){     *
        *     BluetoothGattCharacteristic myChar = gattCharacteristic;          *
        * }                                                                     *
        *************************************************************************

        ####IMPORTANT: All the read, write and enable notification task must be done in a Thread.

        To read a characteristic simply use:

        *************************************************
        * mBluetoothGatt.readCharacteristic(myChar);    *
        *************************************************

        You will receive the read value and result in the mGattCallback above

        To write a characteristic, first set the value and then start the write task. Remember that
        the value must be a byte array

        ***************************************************
        * byte[] mValue = {0x01, 0x02};                   *
        * myChar.setValue(mValue);                        *
        * mBluetoothGatt.writeCharacteristic(myChar);     *
        ***************************************************

        To enable notifications fot a characteristics add:

        ***********************************************************************
        * mBluetoothGatt.setCharacteristicNotification(myChar, true/false);   *
        ***********************************************************************

        You also need to set the client characteristic configuration descriptor 0x2902

        *****************************************************************************
        * UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");      *
        * BluetoothGattDescriptor descriptor = myChar.getDescriptor(uuid);          *
        * descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);   *
        * mBluetoothGatt.writeDescriptor(descriptor);                               *
        *****************************************************************************

        You will receive the notifications in the mGattCallback above.

        A simple thread example to run this code could be this:

        *********************************************************
        * Thread writeThread = new Thread(writeThreadMethod);   *
        * writeThread.start();                                  *
        *********************************************************

        And the writeThreadMethod looks like this:

        *************************************************************
        * Thread writeThreadMethod = new Thread(new Runnable() {    *
        * @Override                                                 *
        * public void run() {                                       *
        *     //Add read, write or enable notifications here        *
        *     runOnUiThread(new Runnable() {                        *
        *         @Override                                         *
        *         public void run() {                               *
        *             //Add UI changes here                         *
        *             }                                             *
        *         });                                               *
        *     }                                                     *
        * });                                                       *
        *************************************************************

        In the next examples we will see how to register and check EID devices.
*/

