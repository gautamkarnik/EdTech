package com.edtech.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by gautamkarnik on 2015-05-19.
 */
public class Settings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String CLASS_NAME = getClass().getSimpleName();

    private static final String SHARED_SETTINGS_PREF = "SHARED_SETTINGS_PREF";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private static final String LANG_SWAHILI = "Swahili";
    private static final String LANG_ENGLISH = "English";

    private static final String MODE_LEARNING = "Learning";
    private static final String MODE_TESTING = "Testing";
    private static final String MODE_USE_ONLINE_LESSONS = "UseOnlineLessons";

    private static final String PROFILE_PIC = "Picture";
    private static final String PROFILE_NAME = "Name";

    protected boolean isFirstTimeLaunch;

    protected boolean swahiliOn;
    protected boolean englishOn;

    protected boolean learningMode;
    protected boolean testingMode;

    protected boolean useOnlineLessons;

    private Context context;
    private SharedPreferences preferences;

    public Settings (Context context) {
        this.context = context;
        preferences =
                context.getSharedPreferences(SHARED_SETTINGS_PREF, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        Log.d(CLASS_NAME, "setFirstTimeLaunch");
        Log.i(CLASS_NAME, "Setting first time launch to " + isFirstTime);
        this.isFirstTimeLaunch = isFirstTime;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, this.isFirstTimeLaunch);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        Log.d(CLASS_NAME, "isFirstTimeLaunch");
        this.isFirstTimeLaunch = preferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
        return this.isFirstTimeLaunch;
    }


    public void setDefaultsIfNotSet() {
        if (!isEnglishOn() && !isSwahiliOn()) {
            Log.d(CLASS_NAME, "Setting default language: English");
            this.setEnglishOn(true);
            this.setSwahiliOn(false);
        }
        if (!isLearningMode() && !isTestingMode()) {
            Log.d(CLASS_NAME, "Setting default mode: Learning");
            this.setLearningMode(true);
            this.setTestingMode(false);
        }
        if (!isUseOnlineLessons()) {
            Log.d(CLASS_NAME, "Setting default mode: Use Online Lessons");
            this.setUseOnlineLessons(false);
        }
    }

    public boolean isSwahiliOn() {
        Log.d(CLASS_NAME, "isSwahiliOn");
        if(preferences.contains(LANG_SWAHILI)) {
            this.swahiliOn = preferences.getBoolean(LANG_SWAHILI, false);
        }
        return this.swahiliOn;
    }

    public void setSwahiliOn(boolean enabled){
        Log.d(CLASS_NAME, "setSwahili");
        Log.i(CLASS_NAME, "Setting language Swahili to " + enabled);
        this.swahiliOn = enabled;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LANG_SWAHILI, this.swahiliOn);
        editor.apply();
    }

    public boolean isEnglishOn() {
        Log.d(CLASS_NAME, "isEnglishOn");
        if(preferences.contains(LANG_ENGLISH)) {
            this.englishOn = preferences.getBoolean(LANG_ENGLISH, false);
        }
        return this.englishOn;
    }

    public void setEnglishOn(boolean enabled){
        Log.d(CLASS_NAME, "setEnglish");
        Log.i(CLASS_NAME, "Setting language English to " + enabled);
        this.englishOn = enabled;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LANG_ENGLISH, this.englishOn);
        editor.apply();
    }

    public boolean isLearningMode() {
        Log.d(CLASS_NAME, "isLearningMode");
        if(preferences.contains(MODE_LEARNING)) {
            this.learningMode = preferences.getBoolean(MODE_LEARNING, false);
        }
        return this.learningMode;
    }

    public void setLearningMode(boolean enabled) {
        Log.d(CLASS_NAME, "setLearningMode");
        Log.i(CLASS_NAME, "Setting learning mode to " + enabled);
        this.learningMode = enabled;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MODE_LEARNING, this.learningMode);
        editor.apply();
    }

    public boolean isTestingMode() {
        Log.d(CLASS_NAME, "isTestingMode");
        if(preferences.contains(MODE_TESTING)) {
            this.testingMode = preferences.getBoolean(MODE_TESTING, false);
        }
        return this.testingMode;
    }

    public void setTestingMode(boolean enabled) {
        Log.d(CLASS_NAME, "setTestingMode");
        Log.i(CLASS_NAME, "Setting testing mode to " + enabled);
        this.testingMode = enabled;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MODE_TESTING, this.testingMode);
        editor.apply();
    }

    public boolean isUseOnlineLessons() {
        Log.d(CLASS_NAME, "isUseOnlineLessons");
        if(preferences.contains(MODE_USE_ONLINE_LESSONS)) {
            this.useOnlineLessons = preferences.getBoolean(MODE_USE_ONLINE_LESSONS, false);
        }
        return this.useOnlineLessons;
    }

    public void setUseOnlineLessons(boolean enabled) {
        Log.d(CLASS_NAME, "setUseOnlineLessons");
        Log.i(CLASS_NAME, "Setting use online lessons " + enabled);
        this.useOnlineLessons = enabled;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MODE_USE_ONLINE_LESSONS, this.useOnlineLessons);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(CLASS_NAME, "The key '" + key + "' was changed");
    }

    public void setProfilePic(Bitmap profilePic) {
        Log.d(CLASS_NAME, "setProfilePic");
        if (profilePic != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PROFILE_PIC, encodeToBase64(profilePic));
            editor.apply();
        }
    }

    public Bitmap getProfilePic() {
        Log.d(CLASS_NAME, "getProfilePic");
        Bitmap bitmap = null;
        if(preferences.contains(PROFILE_PIC)) {
            bitmap = decodeBase64(preferences.getString(PROFILE_PIC, null));
        }
        return bitmap;
    }

    // Source: http://stackoverflow.com/questions/18072448/how-to-save-image-in-shared-preference-in-android-shared-preference-issue-in-a
    private String encodeToBase64(Bitmap image) {
        Log.d(CLASS_NAME, "encodeToBase64");
        Bitmap pic = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d(CLASS_NAME, "imageEncoded");
        return imageEncoded;
    }

    private Bitmap decodeBase64(String input) {
        Log.d(CLASS_NAME, "decodeBase64");
        if (input != null) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } else {
            return null;
        }
    }

    public void setProfileName(String name) {
        Log.d(CLASS_NAME, "setProfileName");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROFILE_NAME, name);
        editor.apply();
    }

    public String getProfileName() {
        Log.d(CLASS_NAME, "getProfileName");
        String name = null;
        if(preferences.contains(PROFILE_NAME)) {
            name = preferences.getString(PROFILE_NAME, null);
        }
        return name;
    }
}