package com.tudelft.iots.ecg.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.Nullable;

import com.tudelft.iots.ecg.database.converter.DateConverter;

import java.util.Arrays;

@Entity(tableName = "activities", indices = { @Index("timestamp_start"), @Index("timestamp_end") })
public class Activity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp_start")
    @TypeConverters(DateConverter.class)
    public long timestamp_start = System.currentTimeMillis();

    @ColumnInfo(name = "timestamp_end")
    @Nullable
    @TypeConverters(DateConverter.class)
    public long timestamp_end = System.currentTimeMillis();

    @ColumnInfo(name = "heart_rate")
    @Nullable
    public int avg_hr;

    @Nullable
    @ColumnInfo(name = "time_zone_0")
    public int zone_0;

    @Nullable
    @ColumnInfo(name = "time_zone_1")
    public int zone_1;

    @Nullable
    @ColumnInfo(name = "time_zone_2")
    public int zone_2;

    @Nullable
    @ColumnInfo(name = "time_zone_3")
    public int zone_3;

    @Nullable
    @ColumnInfo(name = "time_zone_4")
    public int zone_4;

    @Nullable
    @ColumnInfo(name = "time_zone_5")
    public int zone_5;

    @Ignore
    public void setZone(int zone, int seconds){
        switch (zone){
            case 0:
                zone_0 = seconds;
                break;
            case 1:
                zone_1 = seconds;
                break;
            case 2:
                zone_2 = seconds;
                break;
            case 3:
                zone_3 = seconds;
                break;
            case 4:
                zone_4 = seconds;
                break;
            case 5:
                zone_5 = seconds;
                break;
        }
    }

    @Ignore
    public int getMostUsedZone(){
        int[] zones = {zone_0, zone_1, zone_2, zone_3, zone_4, zone_5};
        int[] sorted = zones.clone();
        Arrays.sort(sorted);
        for (int i = 0; i < zones.length; i++) {
            if(zones[i] == sorted[sorted.length - 1]) return i;
        }
        return 0;
    }

    @Ignore
    public int getTimeinZone(int i){
        int[] zones = {zone_0, zone_1, zone_2, zone_3, zone_4, zone_5};
        if(i >= 0 && i < zones.length){
            return zones[i];
        }
        return 0;
    }
}

