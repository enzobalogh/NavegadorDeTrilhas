package com.example.trilhasapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    private static final String PREFS_NAME = "trilhas_app_prefs";

    private static final String KEY_MAP_TYPE = "map_type";

    private static final String KEY_NAVIGATION_MODE = "navigation_mode";

    public static final String MAP_TYPE_VECTOR = "vector";

    public static final String MAP_TYPE_SATELLITE = "satellite";

    public static final String NAVIGATION_NORTH_UP = "north_up";

    public static final String NAVIGATION_COURSE_UP = "course_up";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMapType(String mapType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MAP_TYPE, mapType);
        editor.apply(); 
    }

    public String getMapType() {
        return sharedPreferences.getString(KEY_MAP_TYPE, MAP_TYPE_VECTOR);
    }

    public void saveNavigationMode(String navigationMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAVIGATION_MODE, navigationMode);
        editor.apply();
    }

    public String getNavigationMode() {
        return sharedPreferences.getString(KEY_NAVIGATION_MODE, NAVIGATION_NORTH_UP);
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean isSatelliteMap() {
        return MAP_TYPE_SATELLITE.equals(getMapType());
    }

    public boolean isCourseUp() {
        return NAVIGATION_COURSE_UP.equals(getNavigationMode());
    }
}
