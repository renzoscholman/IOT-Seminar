package com.tudelft.iots.ecg;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tudelft.iots.ecg.classes.MyMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ServiceActivity implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    private LineChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    protected Typeface tfRegular;

    protected float LOWER_LIMIT = 40;
    protected float UPPER_LIMIT = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {
                    updateData(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 1));
                }
            }
        };
        super.onCreate(savedInstanceState);

        if(mDeviceAddress != null){
            Toast.makeText(this, "Started app, device is set: "+mDeviceAddress, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Started app, no device set", Toast.LENGTH_SHORT).show();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getActionBar().setTitle(R.string.title_devices);
        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        setTitle("LineChartActivity1");

        tvX = findViewById(R.id.tvXMax);
        tvY = findViewById(R.id.tvYMax);

        setupChart();
        setupAxis();
        setupSeekBars();
        setupLegend();
    }

    public void updateData(final int i){
        new Thread(new Runnable() {
            public void run() {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                        .fallbackToDestructiveMigration()
                        .build();
                setData(db.heartRateDao().getHeartRatesLimit(i));
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
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));

        //setData(seekBarX.getProgress(), seekBarY.getProgress());

        // redraw
        //
    }

    private void setData(List<HeartRate> hrs){
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < hrs.size(); i++) {
            HeartRate hr = hrs.get(i);
            values.add(new Entry(hr.timestamp, hr.heartRate, getResources().getDrawable(R.drawable.star)));
        }

        setData(values);

        // redraw
        chart.invalidate();
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<>();
        int max = -1, min = 900;

        float lastVal = LOWER_LIMIT;
        for (int i = 0; i < count; i++) {

            float val = lastVal + (float) (Math.random() * 10f - 5f);
            if (val < LOWER_LIMIT) val = LOWER_LIMIT;
            if (val > UPPER_LIMIT) val = UPPER_LIMIT;
            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
            if(val < min) min = (int)val;
            if(val > max) max = (int)val;
            lastVal = val;
        }

        setData(values);

        // redraw
        chart.invalidate();
    }

    private void setData(ArrayList<Entry> values) {
        LineDataSet set1;

        float max = -1.0f, min = 4096.0f;
        for(Entry entry : values){
            if(entry.getY() < min) min = entry.getY();
            if(entry.getY() > max) max = entry.getY();
        }
        setLimits(max, min);

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "Heart Rate");

            set1.setDrawIcons(false);

            // Set cubic filter
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(1f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(0f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
        }
        chart.invalidate();
    }

    protected void saveToGallery() {
        if (chart.saveToGallery("LineChartActivity1_" + System.currentTimeMillis(), 70))
            Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                    .show();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOW HIGH", "low: " + chart.getLowestVisibleX() + ", high: " + chart.getHighestVisibleX());
        Log.i("MIN MAX", "xMin: " + chart.getXChartMin() + ", xMax: " + chart.getXChartMax() + ", yMin: " + chart.getYChartMin() + ", yMax: " + chart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    protected void setupChart(){
        chart = findViewById(R.id.chart1);

        // background color
        chart.setBackgroundColor(Color.WHITE);

        // disable description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // set listeners
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);

        // create marker to display box when values are selected
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // Set the marker to the chart
        mv.setChartView(chart);
        chart.setMarker(mv);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
        chart.setPinchZoom(true);
    }



    private void setupAxis() {
        XAxis xAxis = chart.getXAxis();

        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        YAxis yAxis = chart.getAxisLeft();

        // disable dual axis (only use LEFT axis)
        chart.getAxisRight().setEnabled(false);

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        setLimits(UPPER_LIMIT, LOWER_LIMIT);

        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        llXAxis.setTypeface(tfRegular);

        // draw limit lines behind data instead of on top
        xAxis.setDrawLimitLinesBehindData(true);

        // add limit lines
        //xAxis.addLimitLine(llXAxis);
    }

    protected void setLimits(float upper, float lower){
        LimitLine ll1 = new LimitLine(upper, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(tfRegular);

        LimitLine ll2 = new LimitLine(lower, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(tfRegular);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.removeAllLimitLines();
        yAxis.setDrawLimitLinesBehindData(true);
        yAxis.addLimitLine(ll1);
        yAxis.addLimitLine(ll2);

        // axis range
        yAxis.setAxisMaximum(upper + 10f);
        yAxis.setAxisMinimum(lower - 10f);
    }

    protected void setupSeekBars(){
        seekBarX = findViewById(R.id.seekBar1);
        seekBarX.setOnSeekBarChangeListener(this);

        seekBarY = findViewById(R.id.seekBar2);
        seekBarY.setMax(180);
        seekBarY.setOnSeekBarChangeListener(this);

        // add data
        seekBarX.setProgress(45);
        seekBarY.setProgress(180);
    }

    protected void setupLegend() {
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }
}
