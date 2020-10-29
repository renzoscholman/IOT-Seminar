package com.tudelft.iots.ecg.database.interfaces;

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
}
