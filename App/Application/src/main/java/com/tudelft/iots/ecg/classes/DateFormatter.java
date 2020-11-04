package com.tudelft.iots.ecg.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    SimpleDateFormat mFormat;
    SimpleDateFormat mFormatDate;

    public DateFormatter(){
        mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        mFormatDate = new SimpleDateFormat("EEE, dd MMM, yyyy", Locale.ENGLISH);
    }

    public String format(long timestamp){
        return mFormat.format(new Date(timestamp));
    }

    public String format(long timestamp_start, long timestamp_end){
        return mFormat.format(new Date(timestamp_start)) + " - " + mFormat.format(new Date(timestamp_end));
    }

    public String formatDate(long timestamp){
        return mFormatDate.format(new Date(timestamp));
    }
}
