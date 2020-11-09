package com.tudelft.iots.ecg;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.tudelft.iots.ecg.classes.DateFormatter;
import com.tudelft.iots.ecg.classes.charts.ChartHelper;
import com.tudelft.iots.ecg.database.model.Activity;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.tudelft.iots.ecg.classes.HeartRateZones;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.model.ECG;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivitySingleActivity extends AppCompatActivity {
    protected static String TAG = "ShowTrackedActivityActivity";
    public static final String EXTRAS_ACTIVITY_ID = "ACTIVITY_ID";

    private Activity mActivity;
    private List<ECG> mECG;
    private List<HeartRate> mHRs;
    private ChartHelper chartHelper;

    private int mAge;
    private long mActivityID;

    protected Typeface mTfRegular;

    private LineChart mChart = null;

    List<Integer> zones;

    AppDatabase db;

    final Observer<Activity> activityObserver = new Observer<Activity>() {
        @Override
        public void onChanged(@Nullable Activity activity) {
            mActivity = activity;


            if(mActivity == null || mActivity.id != mActivityID){
                displayNoActivityFoundError();
                finish();
            }

            fillActivityTimes();
            fillActivityZoneTable();
            updateChartXAxis();
            retrieveHrEcgData();
        }
    };

    final Observer<List<ECG>> ecgObserver = new Observer<List<ECG>>() {
        @Override
        public void onChanged(@Nullable List<ECG> ecgs) {
            mECG = ecgs;


            if(mECG == null || mECG.size() == 0){
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
            mHRs = hrs;


            if(mHRs == null || mHRs.size() == 0){
                Log.d(TAG, "Found no heart rates");
                return;
            }
            Log.d(TAG, "Found number of heart rates: "+mHRs.size());

            fillHRData();
            fillActivityHRTable();

            // redraw
            mChart.invalidate();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        final Intent intent = getIntent();
        mActivityID = intent.getLongExtra(EXTRAS_ACTIVITY_ID, -1);
        if(mActivityID == -1){
            Toast.makeText(this, R.string.unknown_activity, Toast.LENGTH_SHORT).show();
            finish();
        }

        getSupportActionBar().setTitle(R.string.title_tracked);

        mAge = 25;
        zones = new HeartRateZones(mAge).getZones();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                .fallbackToDestructiveMigration()
                .build();
        db.activityDao().getActivityById(mActivityID).observe(this, activityObserver);

        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        if(mChart == null){
            chartHelper = new ChartHelper(this);
            mChart = chartHelper.setupChart(R.id.chart1);
            chartHelper.setupAxis(-1);
            chartHelper.setupLegend();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void retrieveHrEcgData(){
        Log.d(TAG, "Retrieve data between: "+mActivity.timestamp_start+ " and "+mActivity.timestamp_end);
        db.ecgDao().getECGsBetween(mActivity.timestamp_start, mActivity.timestamp_end).observe(this, ecgObserver);
        db.heartRateDao().getHeartRatesBetween(mActivity.timestamp_start, mActivity.timestamp_end).observe(this, hrObserver);
    }

    private void fillActivityTimes() {
        DateFormatter mFormat = new DateFormatter();
        TextView dates = findViewById(R.id.activity_date);
        dates.setText(getString(R.string.activity_date, mFormat.formatDate(mActivity.timestamp_start)));
        TextView times = findViewById(R.id.activity_time);
        String text = getString(R.string.activity_time, mFormat.format(mActivity.timestamp_start),mFormat.format(mActivity.timestamp_end));
        times.setText(text);
    }

    private void fillActivityHRTable() {
        class hrCompare implements Comparator<HeartRate> {
            public int compare(HeartRate a, HeartRate b) {
                if (a.heartRate < b.heartRate)
                    return -1; // highest value first
                if (a.heartRate == b.heartRate)
                    return 0;
                return 1;
            }
        }
        ((TextView) findViewById(R.id.activity_hr_min_label)).setText(R.string.activity_hr_min);
        ((TextView) findViewById(R.id.activity_hr_avg_label)).setText(R.string.activity_hr_avg);
        ((TextView) findViewById(R.id.activity_hr_max_label)).setText(R.string.activity_hr_max);
        ((TextView) findViewById(R.id.activity_hr_min)).setText(Collections.min(mHRs, new hrCompare()).heartRate + "/s");
        ((TextView) findViewById(R.id.activity_hr_avg)).setText(mActivity.avg_hr + "/s");
        ((TextView) findViewById(R.id.activity_hr_max)).setText(Collections.max(mHRs, new hrCompare()).heartRate + "/s");
    }

    private void fillActivityZoneTable() {
        String zone_0 = getString(R.string.zone_0, zones.get(0));
        ((TextView) findViewById(R.id.activity_zone_0_label)).setText(zone_0);
        ((TextView) findViewById(R.id.activity_zone_0)).setText(mActivity.zone_0 + "s");
        String zone_1 = getString(R.string.zone_1, zones.get(0), zones.get(1));
        ((TextView) findViewById(R.id.activity_zone_1_label)).setText(zone_1);
        ((TextView) findViewById(R.id.activity_zone_1)).setText(mActivity.zone_1 + "s");
        String zone_2 = getString(R.string.zone_2, zones.get(1), zones.get(2));
        ((TextView) findViewById(R.id.activity_zone_2_label)).setText(zone_2);
        ((TextView) findViewById(R.id.activity_zone_2)).setText(mActivity.zone_2 + "s");
        String zone_3 = getString(R.string.zone_3, zones.get(2), zones.get(3));
        ((TextView) findViewById(R.id.activity_zone_3_label)).setText(zone_3);
        ((TextView) findViewById(R.id.activity_zone_3)).setText(mActivity.zone_3 + "s");
        String zone_4 = getString(R.string.zone_4, zones.get(3), zones.get(4));
        ((TextView) findViewById(R.id.activity_zone_4_label)).setText(zone_4);
        ((TextView) findViewById(R.id.activity_zone_4)).setText(mActivity.zone_4 + "s");
        String zone_5 = getString(R.string.zone_5, zones.get(4));
        ((TextView) findViewById(R.id.activity_zone_5_label)).setText(zone_5);
        ((TextView) findViewById(R.id.activity_zone_5)).setText(mActivity.zone_5 + "s");
    }

    private void displayNoActivityFoundError() {
        Toast.makeText(this, R.string.unknown_activity, Toast.LENGTH_SHORT).show();
    }

    private void setLimits(float upper, float lower){
        LimitLine ll1 = new LimitLine(upper, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(mTfRegular);

        LimitLine ll2 = new LimitLine(lower, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(mTfRegular);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.removeAllLimitLines();
        yAxis.setDrawLimitLinesBehindData(true);
        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);

        // axis range
        upper = Math.max(130.0f, upper);
        lower = Math.min(50.0f, lower);
        yAxis.setAxisMaximum(upper + 10f);
        yAxis.setAxisMinimum(lower - 10f);
    }

    private void updateChartXAxis() {
        XAxis xAxis = mChart.getXAxis();

        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long time = (long)value;
                return mFormat.format(new Date(time + mActivity.timestamp_start));
            }
        });

        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(mActivity.timestamp_end - mActivity.timestamp_start);
    }

    protected void setECGLimits(float upper, float lower){
        YAxis yAxis = mChart.getAxisRight();
        upper = Math.max(4096.0f, upper) + 10.0f;
        lower = Math.min(1536.0f, lower) - 10.0f;

        // axis range
        yAxis.setAxisMaximum(upper);
        yAxis.setAxisMinimum(lower);
    }

    private void fillHRData() {
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < mHRs.size(); i++) {
            HeartRate hr = mHRs.get(i);
            long x = hr.timestamp - mActivity.timestamp_start;
            values.add(new Entry((float)x, hr.heartRate));
        }

        chartHelper.setHRData(values);
        return;

//        LineData data = mChart.getData();
//        LineDataSet set1;
//
//        float max = -1.0f, min = 4096.0f;
//        for(Entry entry : values){
//            if(entry.getY() < min) min = entry.getY();
//            if(entry.getY() > max) max = entry.getY();
//        }
//        setLimits(max, min);
//
//        if(data != null){
//            set1 = (LineDataSet) mChart.getData().getDataSetByLabel("Heart Rate", false);
//            if(set1 != null){
//                set1.setValues(values);
//                set1.notifyDataSetChanged();
//                mChart.getData().notifyDataChanged();
//            } else {
//                set1 = ChartDataSetHelper.getHRDataSet(values);
//                data.addDataSet(set1);
//            }
//        } else {
//            set1 = ChartDataSetHelper.getHRDataSet(values);
//            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//            dataSets.add(set1); // add the data sets
//            data = new LineData(dataSets);
//            mChart.setData(data);
//        }
//        mChart.notifyDataSetChanged();
    }

    private void fillECGData() {
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < mECG.size(); i++) {
            ECG hr = mECG.get(i);
            long x = hr.timestamp - mActivity.timestamp_start;
            values.add(new Entry((float)x, hr.ecg));
        }

        chartHelper.setECGData(values);
        return;

//        LineData data = mChart.getData();
//        LineDataSet set1;
//
//        float max = -1.0f, min = 4096.0f;
//        for(Entry entry : values){
//            if(entry.getY() < min) min = entry.getY();
//            if(entry.getY() > max) max = entry.getY();
//        }
//        setECGLimits(max, min);
//
//        if(data != null){
//            set1 = (LineDataSet) mChart.getData().getDataSetByLabel("ECG", false);
//            if(set1 != null){
//                set1.setValues(values);
//                set1.notifyDataSetChanged();
//                mChart.getData().notifyDataChanged();
//            } else {
//                set1 = ChartDataSetHelper.getECGDataSet(values);
//                data.addDataSet(set1);
//            }
//        } else {
//            set1 = ChartDataSetHelper.getECGDataSet(values);
//            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
//            dataSets.add(set1); // add the data sets
//            data = new LineData(dataSets);
//            mChart.setData(data);
//        }
//        mChart.notifyDataSetChanged();
    }
}