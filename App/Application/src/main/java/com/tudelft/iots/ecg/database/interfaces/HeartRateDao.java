package com.tudelft.iots.ecg.database.interfaces;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.List;

@Dao
public interface HeartRateDao {
    @Insert()
    long insert(HeartRate heartrate);

    @Query("SELECT * FROM heart_rates")
    List<HeartRate> getAllHeartRates();

    @Query("SELECT * FROM heart_rates LIMIT :i")
    List<HeartRate> getHeartRatesLimit(int i);

    @Query("SELECT * FROM heart_rates WHERE timestamp >= :startTime AND timestamp <= :endTime")
    LiveData<List<HeartRate>> getHeartRatesBetween(long startTime, long endTime);

    @Query("SELECT * FROM heart_rates WHERE timestamp >= :startTime AND timestamp <= :endTime")
    List<HeartRate> getHeartRatesAtTime(long startTime, long endTime);

    @Query("SELECT COUNT(*) from heart_rates")
    int count();

    @Query("DELETE FROM heart_rates")
    void clear();
}
