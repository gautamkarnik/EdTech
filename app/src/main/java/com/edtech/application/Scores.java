package com.edtech.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by gautamkarnik on 2015-06-02.
 */
public class Scores implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_SCORE = "Score";
    private static final String UNIT_SCORES = "UNIT_SCORES";

    private Context context;
    private SharedPreferences preferences;

    public Scores(Context context) {
        this.context = context;
        preferences =
                context.getSharedPreferences(UNIT_SCORES, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void setUnitScore(String unit, String title, int score){
        Log.d(CLASS_NAME, "setUnitScore");
        SharedPreferences.Editor editor = preferences.edit();
        String key = unit + ":" + title;
        editor.putInt(key, score);
        editor.apply();
    }

    public int getUnitScore(String unit, String title) {
        Log.d(CLASS_NAME, "getUnitScore");

        int score = 0;
        String key = unit + ":" + title;
        if (preferences.contains(key)) {
            score = preferences.getInt(key, 0);
        }
        return score;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(CLASS_NAME, "The key '" + key + "' was changed");
    }
}
