package com.tudelft.iots.ecg.database.interfaces;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.tudelft.iots.ecg.database.model.ECG;

import java.util.List;

@Dao
public interface ECGDao {
    @Insert()
    long insert(ECG ecg);

    @Query("SELECT * FROM ecgs")
    List<ECG> getAllECGs();

    @Query("SELECT * FROM ecgs WHERE timestamp >= :start LIMIT :i")
    List<ECG> getECGsLimit(long i, long start);

    @Query("SELECT * FROM ecgs WHERE timestamp >= :start and timestamp <= :end")
    LiveData<List<ECG>> getECGsBetween(long start, long end);

    @Query("SELECT COUNT(*) from ecgs")
    int count();

    @Query("DELETE FROM ecgs")
    void clear();
}
