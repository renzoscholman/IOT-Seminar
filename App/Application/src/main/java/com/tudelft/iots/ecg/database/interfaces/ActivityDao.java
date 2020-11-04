package com.tudelft.iots.ecg.database.interfaces;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.tudelft.iots.ecg.database.model.Activity;

import java.util.List;

@Dao
public interface ActivityDao {
    @Insert()
    long insert(Activity activity);

    @Query("SELECT * FROM activities WHERE id = :id")
    LiveData<Activity> getActivityById(long id);

    @Query("SELECT * FROM activities")
    LiveData<List<Activity>> getAllActivities();

    @Query("SELECT * FROM activities WHERE timestamp_start >= :start AND timestamp_end <= :end")
    List<Activity> getActivititesBetween(long start, long end);

    @Query("SELECT COUNT(*) from activities")
    int count();

    @Query("DELETE FROM activities")
    void clear();
}
