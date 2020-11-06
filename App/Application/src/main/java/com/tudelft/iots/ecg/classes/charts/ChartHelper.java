package com.tudelft.iots.ecg.classes.charts;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.tudelft.iots.ecg.R;
import com.tudelft.iots.ecg.classes.MyMarkerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChartHelper {
    private final float LOWER_LIMIT = 40;
    private final float UPPER_LIMIT = 200;

    private AppCompatActivity mContext;

    private boolean mRealtime = false;
    private long mStartTime = -1;

    private LineChart mChart = null;
    private SeekBar seekBarX;
    private TextView tvX;

    protected Typeface tfRegular;

    private long mStartMillis = System.currentTimeMillis();

    public ChartHelper(AppCompatActivity context){
        mContext = context;
        tfRegular = Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf");
    }

    public LineChart setupChart(@IdRes int id, boolean realTime){
        mRealtime = realTime;
        return setupChart(id);
    }

    public LineChart setupChart(@IdRes int id){
        mChart = mContext.findViewById(id);

        // background color
        mChart.setBackgroundColor(Color.WHITE);

        // disable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // set listeners
        mChart.setDrawGridBackground(false);

        // create marker to display box when values are selected
        MyMarkerView mv = new MyMarkerView(mContext, R.layout.custom_marker_view);

        // Set the marker to the chart
        mv.setChartView(mChart);
        mChart.setMarker(mv);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // force pinch zoom along both axis
        mChart.setPinchZoom(true);

        mChart.setMinimumHeight(750);
        mChart.setScaleYEnabled(false);
        mChart.setScaleXEnabled(true);

        return mChart;
    }

    public void setLimits(float upper, float lower, int showSeconds){
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

        if(showSeconds > 0){
            XAxis xAxis = mChart.getXAxis();
            if(mRealtime){
                xAxis.setAxisMinimum(0f);
                xAxis.setAxisMaximum(showSeconds * 1000.0f);
            } else {
                float min = Math.max(0, xAxis.getAxisMaximum() - showSeconds * 1000.0f);
                xAxis.setAxisMinimum(min);
            }
        }
    }

    public void setStartMillis(long startMillis){
        mStartMillis = startMillis;
        XAxis xAxis = mChart.getXAxis();

        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long time = (long)value;
                return mFormat.format(new Date(time + mStartMillis));
            }
        });
    }

    public void setupAxis(int showSeconds) {
        XAxis xAxis = mChart.getXAxis();

        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 10f, 0f);

        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long time = (long)value;
                return mFormat.format(new Date(time + mStartMillis));
            }
        });

        YAxis yAxis = mChart.getAxisLeft();

        mChart.getAxisRight().setEnabled(false);

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 10f, 0f);

        setLimits(UPPER_LIMIT, LOWER_LIMIT, showSeconds);

        LimitLine llXAxis = new LimitLine(9f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);
        llXAxis.setTypeface(tfRegular);

        // draw limit lines behind data instead of on top
        xAxis.setDrawLimitLinesBehindData(true);
    }

    public void setupSeekBars(int showSeconds){
        seekBarX = mContext.findViewById(R.id.seekBar1);
        tvX = mContext.findViewById(R.id.tvXMax);
        seekBarX.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) mContext);

        // add data
        seekBarX.setProgress(showSeconds);
        updateSeekbar(showSeconds);
    }

    public void setupLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }

    public void updateSeekbar(int value){
        tvX.setText(String.valueOf(value));
    }

    protected void setECGLimits(float upper, float lower){
        YAxis yAxis = mChart.getAxisRight();
        upper = Math.max(4096.0f, upper) + 10.0f;
        lower = Math.min(1536.0f, lower) - 10.0f;

        // axis range
        yAxis.setAxisMaximum(upper);
        yAxis.setAxisMinimum(lower);
    }


    public void setHRData(ArrayList<Entry> values) {
        LineData data = mChart.getData();
        LineDataSet set1;

        float max = -1.0f, min = 4096.0f;
        for(Entry entry : values){
            if(entry.getY() < min) min = entry.getY();
            if(entry.getY() > max) max = entry.getY();
        }
        int showSeconds = seekBarX != null ? seekBarX.getProgress() : -1;
        setLimits(max, min, showSeconds);

        if(data != null){
            set1 = (LineDataSet) mChart.getData().getDataSetByLabel("Heart Rate", false);
            if(set1 != null){
                set1.setValues(values);
                set1.notifyDataSetChanged();
                mChart.getData().notifyDataChanged();
            } else {
                set1 = ApplyFill(ChartDataSetHelper.getHRDataSet(values));
                data.addDataSet(set1);
            }
        } else {
            set1 = ApplyFill(ChartDataSetHelper.getHRDataSet(values));
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets
            data = new LineData(dataSets);
            mChart.setData(data);
        }
        mChart.notifyDataSetChanged();
    }

    private LineDataSet ApplyFill(LineDataSet set){
        set.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
            return mChart.getAxisLeft().getAxisMinimum();
            }
        });

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
                @Override
                public Shader resize(int width, int height) {
                    LinearGradient gradient = new LinearGradient(
                            0, mChart.getHeight(), 0, 0,
                            new int[]{Color.GRAY, Color.GRAY, Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED},
                            new float[]{0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f},
                            Shader.TileMode.CLAMP
                    );
                    return gradient;
                }
            };
            PaintDrawable paint = new PaintDrawable();
            paint.setShape(new RectShape());
            paint.setShaderFactory(shaderFactory);

            // drawables only supported on api level 18 and above

            set.setFillDrawable(paint);
            set.setFillAlpha(180);
//            Paint paint = mChart.getRenderer().getPaintRender();
//            paint.setShader(gradient);
//            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//            set.setFillDrawable(drawable);
        } else {
            set.setFillColor(Color.BLACK);
        }

        return set;
    }

    public void setECGData(ArrayList<Entry> values) {
        LineData data = mChart.getData();
        LineDataSet set1;

        float max = -1.0f, min = 4096.0f;
        for(Entry entry : values){
            if(entry.getY() < min) min = entry.getY();
            if(entry.getY() > max) max = entry.getY();
        }
        setECGLimits(max, min);

        if(data != null){
            set1 = (LineDataSet) mChart.getData().getDataSetByLabel("ECG", false);
            if(set1 != null){
                set1.setValues(values);
                set1.notifyDataSetChanged();
                mChart.getData().notifyDataChanged();
            } else {
                set1 = ChartDataSetHelper.getECGDataSet(values);
                data.addDataSet(set1);
            }
        } else {
            set1 = ChartDataSetHelper.getECGDataSet(values);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets
            data = new LineData(dataSets);
            mChart.setData(data);
        }
        mChart.notifyDataSetChanged();
    }
}
