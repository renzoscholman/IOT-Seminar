package com.tudelft.iots.ecg;

import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tudelft.iots.ecg.classes.ActivityListAdapter;
import com.tudelft.iots.ecg.database.AppDatabase;
import com.tudelft.iots.ecg.database.model.Activity;

import java.util.List;

public class ActivityHistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ActivityListAdapter mActivityListAdapter = null;

    AppDatabase db;
    List<Activity> mActivities;


    final Observer<List<Activity>> activityObserver = new Observer<List<Activity>>() {
        @Override
        public void onChanged(@Nullable List<Activity> activities) {
            mActivities = activities;


            if(mActivities == null || mActivities.size() == 0){
                return;
//                displayNoActivityFoundError();
//                finish();
            }

            fillActivities();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_history);
        setContentView(R.layout.activity_history_overview);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "heart-rate-storage")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        if(mActivityListAdapter == null){
            mActivityListAdapter = new ActivityListAdapter(getLayoutInflater());
            ListView listView = findViewById(R.id.listview);
            listView.setAdapter(mActivityListAdapter);
            listView.setOnItemClickListener(this);
        }

        db.activityDao().getAllActivities().observe(this, activityObserver);
    }

    protected void fillActivities(){
        for (Activity a : mActivities){
            mActivityListAdapter.addActivity(a);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityListAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Activity activity = mActivityListAdapter.getActivity(i);
        if (activity == null) return;

        final Intent intent = new Intent(this, ActivitySingleActivity.class);
        intent.putExtra(ActivitySingleActivity.EXTRAS_ACTIVITY_ID, activity.id);
        startActivity(intent);
    }
}
