package com.tudelft.iots.ecg;

import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
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

    protected boolean mChartUpdated = false;

    Thread mUpdateStartTimeThread = null;

    AppDatabase db;

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
        checkPreferences();
        db = AppDatabase.getDatabase(this);

        super.mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {
                    updateDataThreaded();
                }
            }
        };
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.title_devices);
        setupChart();
    }

    private void checkPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!preferences.getBoolean("pref_finished", false)){
            Toast.makeText(this, R.string.notice_preferences, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, PreferenceActivity.class));
            finish();
        }

        boolean hasBLE = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if(hasBLE){
            if(preferences.getString("pref_device_address", null) == null){
                Toast.makeText(this, R.string.notice_hr_monitor, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, DeviceScanActivity.class));
                finish();
            }
        } else {
            Toast.makeText(this, R.string.notice_ble_unsupported, Toast.LENGTH_LONG).show();
        }

        super.enableECG = preferences.getBoolean("pref_use_ecg", false);
    }

    private void setupChart() {
        if(mChart == null){
            SeekBar seekbar = findViewById(R.id.seekBar1);
            showSeconds = seekbar == null ? 10 : seekbar.getProgress();
            chartHelper = new ChartHelper(this);
            mChart = chartHelper.setupChart(R.id.chart1, true);
            chartHelper.setupAxis(showSeconds);
            chartHelper.setupSeekBars(showSeconds);
            chartHelper.setupLegend();
            refreshView();
        }
    }

    @Override
    protected void onDestroy() {
        if(mUpdateStartTimeThread != null){
            mUpdateStartTimeThread.interrupt();
        }
        super.onDestroy();
    }



    public void refreshView(){
        if(updatingChart) return;
        updatingChart = true;

        long length = showSeconds * 1000;
        long currentTime = System.currentTimeMillis();
        long start = currentTime - length;
        Log.d(TAG, String.format("Refreshing view at timestamp: %d", start));

        db.ecgDao().getECGsAfter(start).observe(this, ecgObserver);
        db.heartRateDao().getHeartRatesAfter(start).observe(this, hrObserver);
    }

    public void updateDataThreaded(){
        new Thread(new Runnable() {
            public void run() {
                refreshView();
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
        updateDataThreaded();
    }

    private void updateStartTime(){
        long length = showSeconds * 1000;
        long currentTime = System.currentTimeMillis();
        long start = currentTime - length;
        chartHelper.setStartMillis(start);
        startMillis = start;
    }

    private void fillHRData(){
        if(chart_hrs == null) return;

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < chart_hrs.size(); i++) {
            HeartRate hr = chart_hrs.get(i);
            long x = hr.timestamp - startMillis;
            values.add(new Entry((float)x, hr.heartRate));
        }

        chartHelper.setHRData(values);
        updateStartTime();

        // redraw
        mChart.invalidate();
    }

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
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
