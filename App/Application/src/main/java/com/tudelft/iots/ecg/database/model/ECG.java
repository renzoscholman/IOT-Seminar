package com.tudelft.iots.ecg.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.tudelft.iots.ecg.database.converter.DateConverter;

@Entity(tableName = "ecgs", indices = { @Index("timestamp") })
public class ECG {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    @TypeConverters(DateConverter.class)
    public long timestamp = System.currentTimeMillis();

    @ColumnInfo(name = "ecg")
    public short ecg;
}

