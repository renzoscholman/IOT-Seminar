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

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tudelft.iots.ecg.classes.HeartRateZones;
import com.tudelft.iots.ecg.classes.SampleGattAttributes;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.interfaces.ECGDao;
import com.tudelft.iots.ecg.database.interfaces.HeartRateDao;
import com.tudelft.iots.ecg.database.model.ECG;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private boolean initialized = false;
    private boolean enabledECG = false;

    private long startTimeHR = -1;
    private long startTime = -1;

    private List<HeartRate> lastHeartRates = new ArrayList<>();
    private int hrNotificationId = 0;

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

    // Implements callback methods for GATT events
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            // Reset start times of the connection
            startTimeHR = -1;
            startTime = -1;
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

                // Automatically connect to HR and/or ECG characteristics
                connectToServices(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * Automatically connect to the HR/ECG characteristics by enabling notifications for these
         * @param gatt BluetoothGatt connection object
         */
        private void connectToServices(final BluetoothGatt gatt) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BluetoothGattService ecg_service = gatt.getService(UUID_ECG_SERVICE);
                    if(ecg_service != null){
                        // Enable the notifications for the ECG service
                        BluetoothGattCharacteristic characteristic = ecg_service.getCharacteristic(UUID_ECG_MEASUREMENTS);
                        setCharacteristicNotification(characteristic, true);
                        // Add sleep delay 500 as the bluetooth driver returns immediately after
                        // setting/writing characteristic values and errors occur if we overwrite it
                        try {
                            Thread.sleep(500);
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Enable the heart rate notifications.
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

            // Debug callback to check if correct data is send for power mode
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
            // Callback to log successful connections to characteristics
            if(descriptor.getCharacteristic().getUuid() == UUID_HEART_RATE_MEASUREMENTS){
                Log.d(TAG, "Connected to HR Measurement characteristic");
            }
            if(descriptor.getCharacteristic().getUuid() == UUID_ECG_MEASUREMENTS) {
                Log.d(TAG, "Connected to HR Measurement characteristic");
            }
        }
    };

    /**
     * Sends broadcast based on intent of the action
     * @param action String
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Reads values from the received BluetoothGattCharacteristic and stores its data in the DB and/or
     * transmits broadcasts based on the received data
     *
     * @param action String action to broadcast
     * @param characteristic BluetoothGattCharacteristic to read value from
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID_HEART_RATE_MEASUREMENTS.equals(characteristic.getUuid())) {
            // Extract heart rate and timestamp from packet
            final byte[] data = characteristic.getValue();
            int timestamp = 0;
            for(int i = 0; i < 4; i++){
                timestamp = (timestamp << 8) | data[i] & 0xff;
            }
            final int heartRate = data[4] & 0xff;

            // Log value
            Log.d(TAG, String.format("Received heart rate: %d with ms timestamp: %d", heartRate, timestamp));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));

            // In case timing was not initiated before, do so now.
            if(startTimeHR < 0){
                startTimeHR = System.currentTimeMillis() - timestamp;
            }

            // Store the heart rate in the database, do this in a separate thread in order to not
            // hold up the bluetooth thread
            final HeartRateDao HR = db.heartRateDao();
            final HeartRate hr = new HeartRate();
            hr.heartRate = heartRate;
            hr.timestamp = startTimeHR + timestamp;
            new Thread(new Runnable() {
                public void run() {
                    HR.insert(hr);
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    checkHeartRateValues(hr);
                }
            }).start();
        } else if (UUID_ECG_MEASUREMENTS.equals(characteristic.getUuid())){
            // Extract ECG data from packet
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                // Set start time if not done so before
                if(startTime < 0){
                    startTime = System.currentTimeMillis();
                }
                final ECGDao ECGdao = db.ecgDao();
                // As the packet holds 13 * 1.5 bytes of data packet into a 19.5 byte buffer, unpack accordingly
                for(int i = 0; i < 13; i++){
                    int ecgValue;
                    int start = (int) (i * 1.5);
                    // Bitshift, mask and or the data in the right manner to reconstruct 12 bit value
                    if(i % 2 == 0){
                        ecgValue = (data[start] << 4) | ((data[start + 1] >> 4) & 0xf);
                    } else {
                        ecgValue = ((data[start] & 0xf) << 8) | (data[start + 1] & 0xff);
                    }

                    // Create new database entry and store in a separate thread to not block bluetooth thread
                    final ECG ecg = new ECG();
                    ecg.ecg = (short) ecgValue;
                    ecg.timestamp = startTime + i * 10;
                    new Thread(new Runnable() {
                        public void run() {
                            ECGdao.insert(ecg);
                        }
                    }).start();
                }
                // Increase time by 130 milliseconds (100Hz sampling rate, 13 samples per packet)
                startTime += 130;
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

    private void checkHeartRateValues(HeartRate newHr) {
        while(lastHeartRates.size() >= 5){
            lastHeartRates.remove(0);
        }
        lastHeartRates.add(newHr);
        if (lastHeartRates.size() == 5){
            List<Integer> hrs = new HeartRateZones(this).getZones();
            float total = 0;
            for (HeartRate hr : lastHeartRates){
                total += (float) hr.heartRate / 5.0;
            }
            if(total < (hrs.get(0) * 0.5)){
                showHRNotification(true, Math.round(total));
            } else if (total > hrs.get(hrs.size() - 1)){
                showHRNotification(false, Math.round(total));
            }
        }
    }

    private void showHRNotification(boolean low, int total) {
        int text_resource = low ? R.string.notice_low_hr : R.string.notice_high_hr;
        int title_resource = low ? R.string.notice_low_hr_title : R.string.notice_high_hr_title;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = "Channel Name";
        String channelId = "hr_monitor_channel_1";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(getResources().getString(title_resource))
                .setContentText(getString(text_resource, total))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(text_resource, total)))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // notificationId is a unique int for each notification that you must define
        int id = low ? hrNotificationId : hrNotificationId + 1;
        notificationManager.notify(id, builder.build());
    }

    /**
     * Call this function to trigger another thread for setting the power mode of the ECG device
     *
     * @param enableECG boolean, speaks for itself
     */
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

    /**
     * Sends a value to one of the characteristics on the device to enable ECG data communication
     */
    private void setECG() {
        // Only do this if we are connected of course
        if(mConnectionState == STATE_CONNECTED){
            BluetoothGattService ecg_service = mBluetoothGatt.getService(UUID_ECG_SERVICE);
            if(ecg_service != null){
                BluetoothGattCharacteristic characteristic = ecg_service.getCharacteristic(UUID_ECG_MEASUREMENTS);
                startTime = -1;
                byte val = 49; // Corresponds to balanced power mode (only hr)
                if(enabledECG){
                    val = 50; // Corresponsd to high power mode (hr and ecg)
                }
                Log.d(TAG, String.format("Writing value %d to characteristic", val));
                writeCharacteristicValue(characteristic, val);
            }
        }
    }

    /**
     * Required for the binding to this service by activities
     */
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
        // Do not close the bluetooth connection as other activities might use the same connection
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onDestroy() {
        // Gracefully disconnect and close the bluetooth connection
        disconnect();
        close();
        super.onDestroy();
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if(initialized) return mBluetoothAdapter != null && mBluetoothAdapter != null;
        initialized = true;
        db = AppDatabase.getDatabase(this);

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
     *         is reported asynchronously through the mGattCallback callback.
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

        Log.d(TAG, "Connecting to: "+address);

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
        }
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
        if(address == null) return false;
        return mConnectionState != STATE_DISCONNECTED && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the mGattCallback callback.
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
     * asynchronously through the mGattCallback callback.
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
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, "Enabling HR Notifications");
        }

        // This is specific to ECG.
        if (UUID_ECG_MEASUREMENTS.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
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
