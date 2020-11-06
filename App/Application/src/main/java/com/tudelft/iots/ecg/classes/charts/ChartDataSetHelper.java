package com.tudelft.iots.ecg.classes.charts;

import android.graphics.Color;
import android.graphics.DashPathEffect;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class ChartDataSetHelper {
    public static LineDataSet getECGDataSet(ArrayList<Entry> values){
        LineDataSet set1 = new LineDataSet(values, "ECG");
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set1.setDrawIcons(false);

        // black lines and points
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setCubicIntensity(0.2f);
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
        set1.setDrawFilled(false);
        return set1;
    }

    public static LineDataSet getHRDataSet(ArrayList<Entry> values){
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Heart Rate");

        set1.setDrawIcons(false);

        // Set cubic filter
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setCubicIntensity(0.2f);

        // black lines and points
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);

        // line thickness and point size
        set1.setLineWidth(3f);
        set1.setCircleRadius(5f);

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
        //set1.setDrawFilled(true);
        return set1;
    }
}
