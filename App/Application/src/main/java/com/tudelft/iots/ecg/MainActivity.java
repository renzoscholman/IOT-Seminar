package com.tudelft.iots.ecg;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import com.tudelft.iots.ecg.classes.ToastView;
import com.tudelft.iots.ecg.classes.charts.ChartHelper;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.model.ECG;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ServiceActivity implements SeekBar.OnSeekBarChangeListener {
    private static String TAG = MainActivity.class.getSimpleName();

    private ChartHelper chartHelper;
    private LineChart mChart = null;

    protected int showSeconds;
    protected long startMillis = System.currentTimeMillis();
    protected boolean use_ecg = true;
    protected boolean updatingChart = false;
    protected List<HeartRate> chart_hrs = null;
    protected List<ECG> chart_ecg = null;

    protected LiveData<List<ECG>> ecgs = null;
    protected LiveData<List<HeartRate>> hrs = null;

    protected boolean mChartUpdated = false;

    protected Thread resetQueryThread = null;

    AppDatabase db;

    // Database observer for new ECG values
    final Observer<List<ECG>> ecgObserver = new Observer<List<ECG>>() {
        @Override
        public void onChanged(@Nullable List<ECG> ecgs) {
            if(ecgs == null || ecgs.size() == 0 || !use_ecg){
                Log.d(TAG, "Got no ecg results");
                return;
            }

            chart_ecg = ecgs;
            mChartUpdated = true;
            fillECGData();
            fillHRData();
        }
    };

    // Database observer for new HR values
    final Observer<List<HeartRate>> hrObserver = new Observer<List<HeartRate>>() {
        @Override
        public void onChanged(@Nullable List<HeartRate> hrs) {
            if(hrs == null || hrs.size() == 0){
                Log.d(TAG, "Got no hr results");
                return;
            }

            chart_hrs = hrs;
            mChartUpdated = true;
            fillHRData();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        db = AppDatabase.getDatabase(this);

        // Set receiver for bluetooth updates
        super.mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {
                    updateDataThreaded(); // Refresh view in case of new data
                }
            }
        };
        super.onCreate(savedInstanceState);

        // Setup current view with menu, title and chart layout
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.title_devices);
        setupChart();
    }

    @Override
    protected void onResume() {
        // Check preferences and send user to other activity if required
        checkPreferences();
        super.onResume();

        // Initiate chart reset query thread to periodically reset the queries to preven too much
        // data from being queried.
        resetQueryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Reset the queries every 30 seconds to make sure we dont work with too much data
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Set this value to false to trigger the next call to setQueries to restart the query
                updatingChart = false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Interrupt reset query thread as it is not required by other activities
        resetQueryThread.interrupt();
    }

    /**
     * Checks if the user has finished the setup of the preferences to set his/her age
     * Also checks if the user has connected a device, if not send the user to device setup view
     */
    private void checkPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.getBoolean("pref_finished", false)){
            ToastView.showToast(this, R.string.notice_preferences, 3000);
            startActivity(new Intent(this, PreferenceActivity.class));
            return;
        }

        boolean hasBLE = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if(hasBLE){
            if(preferences.getString("pref_device_address", null) == null){
                ToastView.showToast(this, R.string.notice_hr_monitor, 3000);
                startActivity(new Intent(this, DeviceScanActivity.class));
                return;
            }
        } else {
            Toast.makeText(this, R.string.notice_ble_unsupported, Toast.LENGTH_LONG).show();
        }

        super.enableECG = preferences.getBoolean("pref_use_ecg", false);
    }

    /**
     * Sets up the chart once through the ChartHelper class and initiates the queries
     */
    private void setupChart() {
        if(mChart == null){
            SeekBar seekbar = findViewById(R.id.seekBar1);
            showSeconds = seekbar == null ? 10 : seekbar.getProgress();
            chartHelper = new ChartHelper(this);
            mChart = chartHelper.setupChart(R.id.chart1, true);
            chartHelper.setupAxis(showSeconds);
            chartHelper.setupSeekBars(showSeconds);
            chartHelper.setupLegend();
            setQueries();
        }
    }

    /**
     * Sets the queries once and adds observers to these to monitor changes and refresh the views
     */
    public void setQueries(){
        // Queries use Android LiveData, so only reset these every 30 seconds.
        // This is to prevent too much data being queried and the ui becoming slow
        if(updatingChart) return;
        updatingChart = true;

        long length = showSeconds * 1000;
        long currentTime = System.currentTimeMillis();
        long start = currentTime - length;
        Log.d(TAG, String.format("Refreshing view at timestamp: %d", start));

        // If we are re-instantiating the observers to prevent querying too much data, first remove the old
        if(ecgs != null){
            ecgs.removeObservers(this);
        }
        ecgs = db.ecgDao().getECGsAfter(start);
        ecgs.observe(this, ecgObserver);


        // If we are re-instantiating the observers to prevent querying too much data, first remove the old
        if(hrs != null){
            hrs.removeObservers(this);
        }
        hrs = db.heartRateDao().getHeartRatesAfter(start);
        hrs.observe(this, hrObserver);
    }

    /**
     * Try to reset the queries every time new data is available
     */
    public void updateDataThreaded(){
        new Thread(new Runnable() {
            public void run() {
                setQueries();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_manage_devices) {
            startActivity(new Intent(this, DeviceScanActivity.class));
        }
        if (item.getItemId() == R.id.menu_track_activity) {
            startActivity(new Intent(this, ActivityTrackingActivity.class));
        }
        if (item.getItemId() == R.id.menu_history_activity) {
            startActivity(new Intent(this, ActivityHistoryActivity.class));
        }
        if (item.getItemId() == R.id.menu_preferences) {
            startActivity(new Intent(this, PreferenceActivity.class));
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        showSeconds = seekBar.getProgress();
        chartHelper.updateSeekbar(showSeconds);

        // Set updating to false and restart queries to show new length of chart of showSeconds
        updatingChart = false;
        updateDataThreaded();
    }

    // Updates the start time for the graph
    private void updateStartTime(){
        long length = showSeconds * 1000;
        long currentTime = System.currentTimeMillis();
        long start = currentTime - length;
        chartHelper.setStartMillis(start);
        startMillis = start;
    }

    // Sets the new graph HR data
    private void fillHRData(){
        if(chart_hrs == null) return;

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < chart_hrs.size(); i++) {
            HeartRate hr = chart_hrs.get(i);
            long x = hr.timestamp - startMillis;
            values.add(new Entry((float)x, hr.heartRate));
        }

        chartHelper.setHRData(values);
        if(!super.enableECG){
            chartHelper.setECGData(new ArrayList<Entry>());
        }
        updateStartTime();

        // redraw
        mChart.invalidate();
    }

    // Sets the new graph ECG data
    private void fillECGData(){
        if(chart_ecg == null) return;

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < chart_ecg.size(); i++) {
            ECG ecg = chart_ecg.get(i);
            long x = ecg.timestamp - startMillis;
            values.add(new Entry((float)x, ecg.ecg));
        }

        chartHelper.setECGData(values);
        if(chart_hrs == null){
            updateStartTime();

            // redraw
            mChart.invalidate();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    protected IntentFilter makeGattUpdateIntentFilter() {
        // This activity is only interested in new data
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
