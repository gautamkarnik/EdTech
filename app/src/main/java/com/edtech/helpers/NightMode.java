package com.edtech.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by gautamkarnik on 2015-05-02.
 * Based on source: http://www.androiddevbook.com/ (Chapter 6)
 */
public class NightMode implements SensorEventListener {

    private final String CLASS_NAME = getClass().getSimpleName();
    private final float MIN_LIGHT = SensorManager.LIGHT_SUNRISE;
    private final float NIGHT_LEVEL = 0.1F;
    private final long STOP_RAPID_CHANGES = 30000;
    private float oldLevel = 0.5F;

    private Window window;
    private Activity activity;
    private boolean isLightTheme;
    private boolean useLightSensor;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    private static long themeChanged;
    private float average;

    public NightMode() {
        
    }

    public NightMode(Window window, Activity activity, boolean useLightSensor, boolean isLightTheme) {
        this.window = window;
        this.activity = activity;
        this.useLightSensor = useLightSensor;
        this.isLightTheme = isLightTheme;
    }

    public void on() {
        Log.d(CLASS_NAME, "Turning night mode on");
        WindowManager.LayoutParams params = window.getAttributes();
        oldLevel = params.screenBrightness;
        params.screenBrightness = NIGHT_LEVEL;
        window.setAttributes(params);
    }

    public void off() {
        Log.d(CLASS_NAME, "Turning night mode off");
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = oldLevel;
        window.setAttributes(params);
    }

    public void setDarkTheme() {
        int apiLevel = Build.VERSION.SDK_INT;

        if (!isLightTheme) {
            return;
        }

        //Log.d(CLASS_NAME, "setDarkTheme");
        isLightTheme = false;

        if (apiLevel < 11) {
            changeTheme(android.R.style.Theme_Black);
        } else {
            changeTheme(android.R.style.Theme_Holo);
        }
    }

    public void setLightTheme() {
        int apiLevel = Build.VERSION.SDK_INT;

        if (isLightTheme) {
            return;
        }
        //Log.d(CLASS_NAME, "setLightTheme");
        isLightTheme = true;

        if (apiLevel < 11) {
            changeTheme(android.R.style.Theme_Light);
        } else if (apiLevel < 14) {
            changeTheme(android.R.style.Theme_Holo_Light);
        } else {
            changeTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
        }
    }

    private void changeTheme(int themeID) {
        Intent intent = activity.getIntent();

        this.themeChanged = System.currentTimeMillis();
        //Log.d(CLASS_NAME, "Changing theme time: " + themeChanged);

        intent.putExtra("Theme", themeID);
        intent.putExtra("isLightTheme", isLightTheme);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        activity.overridePendingTransition(0,0);
        activity.finish();

        activity.overridePendingTransition(0,0);
        activity.startActivity(intent);
    }

    public void startSensing() {
        Log.d(CLASS_NAME, "start Sensing");
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensing() {
        Log.d(CLASS_NAME, "stop Sensing");
        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(CLASS_NAME, "Light sensor accuracy changed.");
    }

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float value = event.values[0];
            average = (4 * average + value) / 5;
            //Log.d(CLASS_NAME, "Light sensor is raw " + value + " average " + average);

            long diff = System.currentTimeMillis() - this.themeChanged;
            //Log.d(CLASS_NAME, "diff: " + diff + " themeChanged " + this.themeChanged);

            if (diff > STOP_RAPID_CHANGES) {

                // changing themes does not look great and there are some issues with Holo theme
                // putting themeChanged kludge to support the logic here
                // need to revisit if we like this, for now just set brightness
                this.themeChanged = System.currentTimeMillis();

                if (average <= MIN_LIGHT) {
                    on();
                    // setDarkTheme();
                } else {
                    off();
                    // setLightTheme();
                }

            }
        }
    }

}
