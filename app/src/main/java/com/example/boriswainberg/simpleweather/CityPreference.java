package com.example.boriswainberg.simpleweather;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by boris.wainberg on 12/02/16.
 */
public class CityPreference {
    SharedPreferences prefs;

    public CityPreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity() {
        return prefs.getString("city", "Berlin, DE");
    }

    void setCity(String city) {
        prefs.edit().putString("city", city).commit();
    }
}
