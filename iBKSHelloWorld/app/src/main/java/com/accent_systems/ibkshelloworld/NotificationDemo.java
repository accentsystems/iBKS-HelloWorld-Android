package com.accent_systems.ibkshelloworld;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


public class NotificationDemo extends AppCompatActivity {

    String TAG = "NotificationDemo";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanSettings scanSettings;
    SharedPreferences mPrefs;
    SharedPreferences.Editor edt;
    String mUuid, dRange, mMsg, frameType;
    TextView editBtn;
    Animation animation;
    ImageView wave;
    double dist = 0.5, counter = 0;
    Thread startAnim;

    private Context mContext;

    @Override
    protected void onResume() {
        super.onResume();
        mPrefs = getSharedPreferences("iBKSDemo", Context.MODE_PRIVATE);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.radar_rotate_anim);
        editBtn = (TextView) findViewById(R.id.editBtn);
        editBtn.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim.interrupt();
                wave.setVisibility(View.INVISIBLE);
                wave.clearAnimation();
                showSettingsDialog();
            }
        });

        wave = (ImageView) findViewById(R.id.notifWave);

        loadFilter();

        //image animation of scan
        startAnim = new Thread(loopAnimation);
        startAnim.start();

        //init Bluetooth adapter
        initBT();
        //Start scan of bluetooth devices
        startLeScan(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_notification);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SpannableString s = new SpannableString("Notifications");
        s.setSpan(new com.accent_systems.ibkshelloworld.TypefaceSpan(this, "Khand-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#3a3c3e")), 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setTitle(s);

        getSupportActionBar().setLogo(R.mipmap.ibks);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        startLeScan(false);
        startAnim.interrupt();
        wave.clearAnimation();
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


    Thread loopAnimation = new Thread() {
        @Override
        public void run() {
            try {
                sleep(300);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wave.setVisibility(View.VISIBLE);
                        wave.startAnimation(animation);
                    }
                });
                while(true){
                    if(counter > 0){
                        counter--;
                    }
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //Convert advertising bytes to string for a easier parsing. GetBytes may return a NullPointerException. Treat it right(try/catch).
            String advertisingString = byteArrayToHex(result.getScanRecord().getBytes());
            //Print the advertising String in the LOG with other device info (ADDRESS - RSSI - ADVERTISING - NAME)
            Log.i(TAG, result.getDevice().getAddress()+" - RSSI: "+result.getRssi()+"\t - "+advertisingString+" - "+result.getDevice().getName());

            Log.i(TAG,"UID = "+ mUuid);
            if (advertisingString.contains(mUuid.replace("-",""))) {
                if (result.getRssi() > dist) {
                    if(counter == 0){
                        counter = 5;
                        startAnim.interrupt();
                        wave.clearAnimation();
                        wave.setVisibility(View.INVISIBLE);
                        showDialog();
                    }
                }
            }
        }
    };


    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }


    private void showDialog(){
        final Dialog dialog = new Dialog(NotificationDemo.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_notification);

        TextView text = (TextView) dialog.findViewById(R.id.dialogMsg);
        text.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf"));
        //The message introduced in settings dialog is showed in this dialog
        text.setText(mMsg);

        Button dialogButton = (Button) dialog.findViewById(R.id.closeBtn);
        dialogButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim = new Thread(loopAnimation);
                startAnim.start();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /** Dialog to set the trigger conditions of notification dialog **/
    private void showSettingsDialog(){
        final Dialog dialog = new Dialog(NotificationDemo.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_settings);

        TextView title = (TextView) dialog.findViewById(R.id.settTitle);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));

        TextView uuidTV = (TextView) dialog.findViewById(R.id.settUuid);
        uuidTV.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));

        final Spinner spinneruid = (Spinner) dialog.findViewById(R.id.spinneruid);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));
        spinneruid.setSelection(Integer.parseInt(frameType));


        spinneruid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                final int ft = arg0.getSelectedItemPosition();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        EditText uuidET = (EditText) dialog.findViewById(R.id.uuid);
                        String uid;
                        if(ft == 0) {
                            uid = mPrefs.getString("uuid-ibeacon", "00000000000000000000000000000000-0001-0001");
                        }
                        else
                            uid = mPrefs.getString("uuid-edstuid", "00000000000000000000-000000000001");

                        uuidET.setText(uid);
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //optionally do something here
            }
        });

        final EditText uuid = (EditText) dialog.findViewById(R.id.uuid);
        uuid.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf"));

        uuid.setText(mUuid);

        TextView rannge = (TextView) dialog.findViewById(R.id.settRange);
        rannge.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));

        final Spinner dstt = (Spinner) dialog.findViewById(R.id.distSpinner);
        if(dRange.equals("Immediate")){
            dstt.setSelection(0);
        }else if(dRange.equals("Near")){
            dstt.setSelection(1);
        }else{
            dstt.setSelection(2);
        }

        TextView msgg = (TextView) dialog.findViewById(R.id.settMsg);
        msgg.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));

        final EditText msgt = (EditText) dialog.findViewById(R.id.msgg);
        msgt.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf"));
        msgt.setText(mMsg);
        msgt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (null != msgt.getLayout() && msgt.getLayout().getLineCount() > 5) {
                    msgt.getText().delete(msgt.getText().length() - 1, msgt.getText().length());
                }
            }
        });

        final TextView err = (TextView) dialog.findViewById(R.id.errorMsg);
        err.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf"));

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelBtn);
        cancelButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim = new Thread(loopAnimation);
                startAnim.start();
                dialog.dismiss();
            }
        });

        Button saveButton = (Button) dialog.findViewById(R.id.saveBtn);
        saveButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Khand-Bold.ttf"));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOK=true;
                edt = mPrefs.edit();
                if(spinneruid.getSelectedItemPosition() == 0) {
                    if (uuid.getText().length() != 42) {
                        isOK=false;
                    }
                    else
                        edt.putString("uuid-ibeacon",uuid.getText().toString());
                }else{
                    if (uuid.getText().length() != 33){
                        isOK=false;
                    }
                    else
                        edt.putString("uuid-edstuid",uuid.getText().toString());
                }

                if(isOK){
                    edt.putString("frametype", Integer.toString(spinneruid.getSelectedItemPosition()));
                    edt.putString("range", dstt.getSelectedItem().toString());
                    edt.putString("msg", msgt.getText().toString());
                    edt.commit();
                    loadFilter();
                    err.setVisibility(View.GONE);
                        startAnim = new Thread(loopAnimation);
                       startAnim.start();
                    dialog.dismiss();
                }else {
                    err.setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.show();
    }

    //get parameters saved on SharedPreferences
    private void loadFilter(){
        frameType = mPrefs.getString("frametype","0");

        if(frameType.equals("0"))
            mUuid = mPrefs.getString("uuid-ibeacon", "00000000000000000000000000000000-0001-0001");
        else
            mUuid = mPrefs.getString("uuid-edstuid", "00000000000000000000-000000000001");

        dRange = mPrefs.getString("range", "Immediate");
        mMsg = mPrefs.getString("msg", "Hello World from Accent Systems!");

        if(dRange.equals("Immediate")){
            dist = -38;
        }else if(dRange.equals("Near")) {
            dist = -60;
        }else if(dRange.equals("Far")){
            dist = -120;
        }
    }

}
