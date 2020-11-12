package com.tudelft.iots.ecg;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tudelft.iots.ecg.classes.lists.ActivityListAdapter;
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
            if(activities == null || activities.size() == 0){
                displayNoActivityFoundError();
                finish();
            }

            mActivities = activities;
            fillActivities();
        }
    };

    private void displayNoActivityFoundError() {
        Toast.makeText(this, R.string.notice_no_activity_history, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_history);
        setContentView(R.layout.activity_history_overview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = AppDatabase.getDatabase(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
