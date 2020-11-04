package com.tudelft.iots.ecg;

import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
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
    private ChartHelper chartHelper;
    private LineChart mChart = null;

    protected int showSeconds;
    protected long startMillis = System.currentTimeMillis();
    protected boolean use_ecg = false;
    protected List<HeartRate> chart_hrs = null;
    protected List<ECG> chart_ecg = null;

    AppDatabase db;

    final Observer<List<ECG>> ecgObserver = new Observer<List<ECG>>() {
        @Override
        public void onChanged(@Nullable List<ECG> ecgs) {
            chart_ecg = ecgs;


            if(chart_ecg == null || chart_ecg.size() == 0){
                return;
            }

            fillECGData();

            // redraw
            mChart.invalidate();
        }
    };

    final Observer<List<HeartRate>> hrObserver = new Observer<List<HeartRate>>() {
        @Override
        public void onChanged(@Nullable List<HeartRate> hrs) {
            chart_hrs = hrs;


            if(chart_hrs == null || chart_hrs.size() == 0){
                return;
            }

            fillHRData();

            // redraw
            mChart.invalidate();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                .fallbackToDestructiveMigration()
                .build();

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

        if (mDeviceAddress != null) {
            Toast.makeText(this, "Started app, device is set: " + mDeviceAddress, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Started app, no device set", Toast.LENGTH_SHORT).show();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle(R.string.title_devices);


        if(mChart == null){
            showSeconds = 10;
            chartHelper = new ChartHelper(this);
            mChart = chartHelper.setupChart(R.id.chart1);
            chartHelper.setupAxis(showSeconds);
            chartHelper.setupSeekBars(showSeconds);
            chartHelper.setupLegend();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void refreshView(){
        long length = showSeconds * 1000 + 5000;
        long currentTime = System.currentTimeMillis();
        long start = currentTime - length;

        if(use_ecg){
            db.ecgDao().getECGsBetween(start, currentTime).observe(this, ecgObserver);
        }
        db.heartRateDao().getHeartRatesBetween(start, currentTime).observe(this, hrObserver);
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
        if (item.getItemId() == R.id.menu_enable_ecg) {
            use_ecg = !use_ecg;
            if(use_ecg){
                item.setTitle(R.string.menu_disable_ecg);
                // @todo call bluetooth to enable ecg sending
            } else {
                item.setTitle(R.string.menu_enable_ecg);
                chartHelper.setECGData(new ArrayList<Entry>());
                mChart.invalidate();
                // @todo call bluetooth to disable ecg sending
            }
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
        if(use_ecg && chart_ecg != null){
            if(chart_ecg.size() != 0){
                if(chart_hrs.size() != 0) startMillis = Math.min(chart_ecg.get(0).timestamp, chart_hrs.get(0).timestamp);
                else startMillis = chart_ecg.get(0).timestamp;
            }
        } else {
            if(chart_hrs.size() != 0) startMillis = chart_hrs.get(0).timestamp;
        }
    }

    private void fillHRData(){
        if(chart_hrs == null) return;

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < chart_hrs.size(); i++) {
            HeartRate hr = chart_hrs.get(i);
            long x = hr.timestamp - startMillis;
            values.add(new Entry((float)x, hr.heartRate));
        }

        updateStartTime();
        chartHelper.setHRData(values);
    }

    private void fillECGData(){
        if(chart_ecg == null) return;

        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < chart_ecg.size(); i++) {
            ECG ecg = chart_ecg.get(i);
            long x = ecg.timestamp - startMillis;
            values.add(new Entry((float)x, ecg.ecg));
        }

        updateStartTime();
        chartHelper.setECGData(values);
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
