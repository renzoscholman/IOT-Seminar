package com.tudelft.iots.ecg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PreferenceActivity extends android.preference.PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
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
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String age = preferences.getString("pref_user_age", "-1");
        if(age != null && Integer.parseInt(age) > 0){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("pref_finished", true);
            editor.apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}