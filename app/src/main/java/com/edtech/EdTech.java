package com.edtech;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.edtech.application.Scores;
import com.edtech.application.Settings;
import com.edtech.services.MicrophoneService;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by gautamkarnik on 2015-05-19.
 */
public class EdTech extends Application {
    private final String CLASS_NAME = getClass().getSimpleName();

    private MicrophoneService microphoneService;
    protected Settings settings;
    protected Scores scores;
    private Tracker tracker;


    @Override
    public void onCreate() {
        Log.d(CLASS_NAME, "Application: onCreate");

        super.onCreate();

        Intent service = new Intent(getApplicationContext(), MicrophoneService.class);
        bindService(service, new MicrophoneServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private class MicrophoneServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(CLASS_NAME, "onServiceConnected");

            MicrophoneService.MicrophoneBinder binder = (MicrophoneService.MicrophoneBinder) service;
            microphoneService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(CLASS_NAME, "onServiceDisconnected");
        }
    }

    public MicrophoneService getMicrophoneService() {
        return microphoneService;
    }

    public Settings getSettings() {
         if (settings == null) {
             settings = new Settings(getApplicationContext());
         }
         return settings;
    }

    public void setSettings(Settings settings) {
         this.settings = settings;
     }

    public Scores getScores() {
        if (scores == null) {
            scores = new Scores(getApplicationContext());
        }
        return scores;
    }

    public void setScore(Scores scores) {
        this.scores = scores;
    }


    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
