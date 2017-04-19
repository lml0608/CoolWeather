package com.coolweather.android.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by lfs-ios on 2017/4/19.
 */

public class SpUtil {


    private static final String PREF_DEFAULT_WEATHER = "weather";


    public static String getStoredWeather(Context context) {


        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_DEFAULT_WEATHER, null);

    }

    public static void setStoredWeather(Context context, String weather) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_DEFAULT_WEATHER, weather)
                .commit();
    }
}
