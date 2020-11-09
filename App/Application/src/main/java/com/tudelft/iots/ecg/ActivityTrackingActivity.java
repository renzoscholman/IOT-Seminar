package com.tudelft.iots.ecg;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.tudelft.iots.ecg.classes.HeartRateZones;
import com.tudelft.iots.ecg.classes.charts.ChartHelper;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.model.Activity;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.ArrayList;
import java.util.List;

public class ActivityTrackingActivity extends ServiceActivity {
    private ChartHelper chartHelper;
    private LineChart mChart = null;
    protected Typeface tfRegular;
    Button lastActivity;

    protected boolean tracking = false;
    protected Activity current_activity = null;

    protected List<HeartRate> chart_hrs = null;
    protected List<HeartRate> tracked_hrs = null;

    long last_activity_id = -1;
    protected int AGE = 25;

    AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {
                    updateDataThreaded();
                }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        if (mDeviceAddress != null) {
            Toast.makeText(this, "Started app, device is set: " + mDeviceAddress, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Started app, no device set", Toast.LENGTH_SHORT).show();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setTitle(R.string.title_tracking);
        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        setTitle("Activity Tracking");

        setupButtons();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                .fallbackToDestructiveMigration()
                .build();

        if(mChart == null){
            chartHelper = new ChartHelper(this);
            mChart = chartHelper.setupChart(R.id.chart1);
            chartHelper.setupAxis(-1);
            chartHelper.setupLegend();
        }
    }

    private void setupButtons(){
        final Button startStop = findViewById(R.id.button_start_stop_tracking);
        startStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startStopTracking();
                if(tracking){
                    startStop.setText(R.string.stop_tracking);
                } else {
                    startStop.setText(R.string.start_tracking);
                }
            }
        });

        lastActivity = findViewById(R.id.button_show_last_activity);
        final Intent intent = new Intent(this, ActivitySingleActivity.class);
        lastActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(last_activity_id > 0){
                    intent.putExtra(ActivitySingleActivity.EXTRAS_ACTIVITY_ID, last_activity_id);
                    startActivity(intent);
                }
            }
        });
        lastActivity.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(current_activity != null){
            startStopTracking();
        }
        db.close();
    }

    @Override
    public void onBackPressed() {
        if(tracking){
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit? This will stop the current activity from being tracked.")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            ActivityTrackingActivity.super.onBackPressed();
                        }
                    }).create().show();
        } else {
            super.onBackPressed();
        }
    }

    public void startStopTracking(){
        if(current_activity != null && !tracking){
            Toast.makeText(this, R.string.notice_wait_activity_processing, Toast.LENGTH_LONG).show();
        }

        tracked_hrs = chart_hrs;
        tracking = !tracking;
        if(tracking){
            startActivity();
        } else {
            if (current_activity != null){
                new Thread(new Runnable() {
                    public void run() {
                        stopActivity();
                    }
                }).start();
            }
        }
    }

    public void startActivity(){
        current_activity = new Activity();
        current_activity.timestamp_start = System.currentTimeMillis();

        if(mChart != null){
            mChart.setAlpha(1);
        }
    }

    public void stopActivity(){
        if(mChart != null){
            mChart.setAlpha(0);
        }

        if(tracked_hrs == null){
            return;
        }

        if(tracked_hrs.size() == 0){
            return;
        }

        current_activity.timestamp_end = System.currentTimeMillis();
        int numzones = 6;
        List<List<HeartRate>> zones = new ArrayList<>(numzones);
        for(int i = 0; i < numzones; i++)  {
            zones.add(new ArrayList<HeartRate>());
        }
        List<Integer> hrZones = new HeartRateZones(AGE).getZones();
        for(HeartRate hr : tracked_hrs){
            boolean added = false;
            for(int i = 0; i < hrZones.size(); i++){
                if(hr.heartRate < hrZones.get(i)){
                    added = true;
                    zones.get(i).add(hr);
                    break;
                }
            }
            if(!added) zones.get(5).add(hr);
        }
        long total = 0;
        for (int i = 0; i < zones.size(); i++){
            List<HeartRate> zone = zones.get(i);
            float seconds = 0;
            for (HeartRate hr : zone){
                total += hr.heartRate;
                seconds += (60.0 / hr.heartRate);
            }
            current_activity.setZone(i, Math.round(seconds));
        }
        current_activity.avg_hr = (int) (total / tracked_hrs.size());
        last_activity_id = db.activityDao().insert(current_activity);
        current_activity = null;
        updateLastActivityButton();
    }

    private void updateLastActivityButton(){
        if(last_activity_id > 0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastActivity.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void refreshView(){
        if(current_activity == null) return;
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                .fallbackToDestructiveMigration()
                .build();
        chart_hrs = db.heartRateDao().getHeartRatesAtTime(current_activity.timestamp_start, System.currentTimeMillis());
        db.close();
        runOnUiThread(new Runnable() {
            public void run() {
                setHRData(chart_hrs);
                // redraw
                mChart.invalidate();
            }
        });
    }

    public void updateDataThreaded(){
        new Thread(new Runnable() {
            public void run() {
                if(tracking) refreshView();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            onBackPressed();
        }
        return true;
    }

    private void setHRData(List<HeartRate> hrs){
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < hrs.size(); i++) {
            HeartRate hr = hrs.get(i);
            values.add(new Entry(i, hr.heartRate, hr.timestamp));
        }

        chartHelper.setHRData(values);
    }

    @Override
    protected IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}