package com.tudelft.iots.ecg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class PreferenceActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private static String TAG = PreferenceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create new fragment based on preferences xml
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        // Enable support action bar with back arrow button to previous screen
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Should officially add this, but this app aint that advanced....
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_preference);
        }
    }

    @Override
    public void onBackPressed() {
        // Check if user has successfully set his/her age and if so flag preferences as finished
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String age = preferences.getString("pref_user_age", "-1");
        boolean finished = preferences.getBoolean("pref_finished", false);
        if(!finished && age != null && Integer.parseInt(age) > 0){
            Log.d(TAG, "Setting preferences to finished");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("pref_finished", true);
            editor.apply();
        }
        super.onBackPressed();
    }
}