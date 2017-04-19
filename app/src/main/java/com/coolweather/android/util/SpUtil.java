package com.coolweather.android.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by lfs-ios on 2017/4/19.
 */

public class SpUtil {


    private static final String PREF_DEFAULT_WEATHER = "weather";

    private static final String BING_PIC = "bing_pic";


    public static String getBingPic(Context context) {


        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(BING_PIC, null);

    }

    public static void setBingPic(Context context, String bingpic) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(BING_PIC, bingpic)
                .commit();
    }
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
