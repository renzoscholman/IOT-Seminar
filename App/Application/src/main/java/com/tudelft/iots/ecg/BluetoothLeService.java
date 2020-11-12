/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tudelft.iots.ecg;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tudelft.iots.ecg.classes.SampleGattAttributes;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.interfaces.ECGDao;
import com.tudelft.iots.ecg.database.interfaces.HeartRateDao;
import com.tudelft.iots.ecg.database.model.ECG;
import com.tudelft.iots.ecg.database.model.HeartRate;

//import org.eclipse.paho.android.service.MqttAndroidClient;
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    SharedPreferences preferences;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    //private MqttAndroidClient mMQTTClient;
    private boolean initialized = false;
    private boolean enabledECG = false;

    private long startTimeHR = -1;
    private long startTime = -1;

    AppDatabase db;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_AVAILABLE_ECG =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_ECG";
    public final static String ACTION_DATA_AVAILABLE_HR =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE_HR";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_SERVICE =
            UUID.fromString(SampleGattAttributes.HEART_RATE_SERVICE);

    public final static UUID UUID_HEART_RATE_MEASUREMENTS =
            UUID.fromString(SampleGattAttributes.HEART_RATE_CHARACTERISTIC);

    public final static UUID UUID_ECG_SERVICE =
            UUID.fromString(SampleGattAttributes.ECG_SERVICE);

    public final static UUID UUID_ECG_MEASUREMENTS =
            UUID.fromString(SampleGattAttributes.ECG_MEASUREMENTS);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                connectToServices(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        private void connectToServices(final BluetoothGatt gatt) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BluetoothGattService ecg_service = gatt.getService(UUID_ECG_SERVICE);
                    if(ecg_service != null){
                        BluetoothGattCharacteristic characteristic = ecg_service.getCharacteristic(UUID_ECG_MEASUREMENTS);
                        setCharacteristicNotification(characteristic, true);
                        //add sleep delay 500
                        try {
                            Thread.sleep(500);
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        byte val = 1;
                        if(enabledECG){
                            val = 2;
                        }
                        writeCharacteristicValue(characteristic, val);
                    }

                    //add sleep delay 500
                    try {
                        Thread.sleep(500);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    BluetoothGattService hr_service = gatt.getService(UUID_HEART_RATE_SERVICE);
                    if(hr_service != null){
                        BluetoothGattCharacteristic characteristic = hr_service.getCharacteristic(UUID_HEART_RATE_MEASUREMENTS);
                        setCharacteristicNotification(characteristic, true);
                    }
                }
            }).start();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, String.format("Written to characteristic: %s (status %d)", characteristic.getUuid(), status));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(descriptor.getCharacteristic().getUuid() == UUID_HEART_RATE_MEASUREMENTS){
                Log.d(TAG, "Connected to HR Measurement characteristic");
            }
            if(descriptor.getCharacteristic().getUuid() == UUID_ECG_MEASUREMENTS) {
                Log.d(TAG, "Connected to HR Measurement characteristic");
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdatei(final String action, long i) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, i);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENTS.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            int timestamp = 0;
            for(int i = 0; i < 4; i++){
                timestamp = (timestamp << 8) | data[i] & 0xff;
            }
            final int heartRate = data[4] & 0xff;
            Log.d(TAG, String.format("Received heart rate: %d with ms timestamp: %d", heartRate, timestamp));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
            if(startTimeHR < 0){
                startTimeHR = System.currentTimeMillis() - timestamp;
            }
            final HeartRateDao HR = db.heartRateDao();
            final HeartRate hr = new HeartRate();
            hr.heartRate = heartRate;
            hr.timestamp = startTimeHR + timestamp;
            new Thread(new Runnable() {
                public void run() {
                    HR.insert(hr);
                }
            }).start();
        } else if (UUID_ECG_MEASUREMENTS.equals(characteristic.getUuid())){
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                if(startTime < 0){
                    startTime = System.currentTimeMillis();
                }
                final ECGDao ECGdao = db.ecgDao();
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(int i = 0; i < 13; i++){
                    int ecgValue;
                    int start = (int) (i * 1.5);
                    if(i % 2 == 0){
                        ecgValue = (data[start] << 4) | ((data[start + 1] >> 4) & 0xf);
                    } else {
                        ecgValue = ((data[start] & 0xf) << 8) | (data[start + 1] & 0xff);
                    }

                    final ECG ecg = new ECG();
                    ecg.ecg = (short) ecgValue;
                    ecg.timestamp = startTime + i * 10;
                    new Thread(new Runnable() {
                        public void run() {
                            ECGdao.insert(ecg);
                        }
                    }).start();
                }
                startTime += 130;
                //Log.d(TAG, String.format("Received ECG measurements, current start time: %d", startTime));
            }
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public void enableECG(boolean enableECG) {
        if(enableECG != enabledECG){
            enabledECG = enableECG;
            new Thread(new Runnable(){
                @Override
                public void run() {
                    setECG();
                }
            }).start();
        }
    }

    private void setECG() {
        if(mConnectionState == STATE_CONNECTED){
            BluetoothGattService ecg_service = mBluetoothGatt.getService(UUID_ECG_SERVICE);
            if(ecg_service != null){
                BluetoothGattCharacteristic characteristic = ecg_service.getCharacteristic(UUID_ECG_MEASUREMENTS);
                startTime = -1;
                byte val = 49;
                if(enabledECG){
                    val = 50;
                }
                Log.d(TAG, String.format("Writing value %d to characteristic", val));
                writeCharacteristicValue(characteristic, val);
            }
        }
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public int initDB(){
        db = AppDatabase.getDatabase(this);

        startTime = db.insertDefaultValues();
        return 0;
    }

    @Override
    public void onDestroy() {
        disconnect();
        close();
        super.onDestroy();
    }

    public void threadedTestData(){
        initDB();
        long i = 1;
        int length = db.ecgDao().count();
        int perSec = 10;
        long mult = 1000 / perSec;
        while(i < (length) / perSec){
            try {
                TimeUnit.MILLISECONDS.sleep(1000/perSec);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            broadcastUpdatei(ACTION_DATA_AVAILABLE, startTime + i*mult);
            i++;
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if(initialized) return true;
        initialized = true;
        db = AppDatabase.getDatabase(this);

//        mMQTTClient = new MqttAndroidClient(this.getApplicationContext(), "ADDRESS", "ID");
//        try{
//            Log.i("Connection", "Starting connection ");
//            IMqttToken token = mMQTTClient.connect();
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i("Connection", "success ");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.e("Connection", "Failed "+ Arrays.toString(exception.getStackTrace()));
//                }
//            });
//        } catch (MqttException error){
//            Log.e("Connection", "Error "+ Arrays.toString(error.getStackTrace()));
//        }

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        // Try to establish a connection to previously saved device
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String address = preferences.getString(getString(R.string.preference_device_address), null);
        if(address != null){
            connect(address);
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return false;
        }

        if(address == null){
            Log.w(TAG, "Unspecified address.");
        }

        // Previously connected device.  Try to reconnect.
        if (isConnectedTo(address)) {
            return true;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        if(!address.equals(mBluetoothDeviceAddress)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.preference_device_address), address);
            editor.apply();
        }
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public boolean isConnectedTo(String address){
        return mConnectionState != STATE_DISCONNECTED && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Writes value on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param value string to send to characteristic
     */
    public void writeCharacteristicValue(BluetoothGattCharacteristic characteristic, byte value) {
        if(characteristic == null){
            Log.d(TAG, "Null characteristic given, cannot write");
            return;
        }
        int properties = characteristic.getProperties();
        if (((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) |
            (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
            // writing characteristic functions
            byte[] data = {value};
            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        } else {
            Log.d(TAG, "Characteristic does not have write property");
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENTS.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, "Enabling HR Notifications");
        }

        // This is specific to Heart Rate Measurement.
        if (UUID_ECG_MEASUREMENTS.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, "Enabling ECG Notifications");
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
