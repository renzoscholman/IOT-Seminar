package com.tudelft.iots.ecg.classes;

import java.util.ArrayList;
import java.util.List;

public class HeartRateZones {
    private float mAge;

    public HeartRateZones(int age){
        mAge = (float) age;
    }

    public List<Integer> getZones(){
        List<Integer> zones = new ArrayList<>(6);
        for(float i = 0; i < 6; i++){
            zones.add((int)Math.round((220.0 - mAge) * ((5.0 + i) / 10.0)));
        }
        return zones;
    }
}
