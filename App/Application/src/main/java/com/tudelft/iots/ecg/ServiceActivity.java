package com.tudelft.iots.ecg;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public abstract class ServiceActivity extends AppCompatActivity {
    protected static String TAG = "ServiceActivity";

    protected BluetoothLeService mBluetoothLeService;

    protected BroadcastReceiver mGattUpdateReceiver;
    protected boolean serviceConnected = false;

    protected String mDeviceName;
    protected String mDeviceAddress;

    protected boolean forceReconnect = false;
    protected boolean enableECG = false;
    protected boolean ecgEnabled = false;

    // Code to manage Service lifecycle.
    protected final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.enableECG(enableECG);

            if(mDeviceAddress == null){
                Log.e(TAG, "No device address given");
            } else {
                if (mDeviceAddress.length() == 0) {
                    Log.e(TAG, "No device address given");
                } else {
                    // Automatically connects to the device upon successful start-up initialization.
                    new Thread(new Runnable() {
                        public void run() {
                        mBluetoothLeService.connect(mDeviceAddress);
                        }
                    }).start();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if(mGattUpdateReceiver != null){
            unregisterReceiver(mGattUpdateReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            if(mDeviceAddress == null){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                mDeviceAddress = preferences.getString(getString(R.string.preference_device_address), "");
            }
            if(mDeviceAddress != null && mDeviceAddress.length() > 0){
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        enableECG = preferences.getBoolean("pref_use_ecg", false);
        Log.d(TAG, String.format("Read ECG preference value: %b", enableECG));

        if(serviceConnected && mBluetoothLeService != null){
            mBluetoothLeService.enableECG(enableECG);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            serviceConnected = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceConnected){
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    protected IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
