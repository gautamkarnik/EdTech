package com.edtech.activities;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edtech.AppPermissions;
import com.edtech.Constants;
import com.edtech.EdTech;
import com.edtech.R;
import com.edtech.application.Scores;
import com.edtech.fragments.AbcFragmentTab;
import com.edtech.fragments.BaseFragmentTab;
import com.edtech.fragments.LessonsFragmentTab;
import com.edtech.fragments.MathFragmentTab;
import com.edtech.fragments.SpeakFragmentTab;
import com.edtech.helpers.NightMode;
import com.edtech.application.Settings;
import com.edtech.model.Syllabus;
import com.edtech.utilities.LessonAsyncHttpReader;
import com.edtech.utilities.LessonParser;
import com.edtech.utilities.LessonReader;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class MainActivity extends AppCompatActivity implements ActionBar.TabListener,
        LessonsFragmentTab.OnUnitSelectedListener, TextToSpeech.OnInitListener,
        BaseFragmentTab.SlateInteractions, View.OnClickListener {

    private final String CLASS_NAME = getClass().getSimpleName();

    private final int MY_DATA_CHECK_CODE = 0;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    CustomViewPager mViewPager;

    private NightMode mNightModeHelper;
    private Syllabus mSyllabus;
    private Settings mSettings;
    private Scores mScores;
    private TextToSpeech ttsObj;

    private LessonManagerTask mAsyncLessonThread = null;
    private BroadcastReceiver mReceiver = null;

    private FrameLayout mFlashBar;
    private ImageButton mNextButton;
    private ProgressBar mProgress;
    private TextView mScoreText;
    private TextView mCounterText;
    private CountDownTimer mCountDownTimer;

    private ImageView mStickerView;
    private AnimationDrawable mStickerFrameAnimation;

    private AppPermissions appPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appPermissions = new AppPermissions(this);

        if (!appPermissions.checkPermissionForRecordAudio() ||
            !appPermissions.checkPermissionForCamera() ||
            !appPermissions.checkPermissionForWriteExternalStorage()) {
            appPermissions.requestPermissions();
        }

        int themeID = getIntent().getIntExtra("Theme", -1);
        if (themeID != -1) {
            setTheme(themeID);
        }

        boolean isLightTheme = getIntent().getBooleanExtra("isLightTheme", true);

        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setIcon(R.mipmap.ic_launcher);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);

                // Obtain the shared Tracker instance and page number
                Tracker tracker = ((EdTech) getApplication()).getDefaultTracker();
                String name = mViewPager.getAdapter().getPageTitle(position).toString();
                tracker.setScreenName(Constants.VIEW_TAB + name);
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }
        });


        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_lessons).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_abc).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_123).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_mic).setTabListener(this));

        mStickerView = (ImageView) findViewById(R.id.stickerView);
        mStickerView.setVisibility(View.GONE);

        mFlashBar = (FrameLayout) findViewById(R.id.FlashBarLayout);
        mFlashBar.setVisibility(View.GONE);

        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(this);

        mScoreText = (TextView) findViewById(R.id.textScore);
        mCounterText = (TextView) findViewById(R.id.textTimer);

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setProgress(0);

        mNightModeHelper = new NightMode(getWindow(), this, true, isLightTheme);

        // create EdTech Folder if it doesn't exist
        File folder = new File(Constants.EDTECH_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // delete any old audio files
        MainActivityHelper.deleteFileIfExists(Constants.TTS_AUDIO_TEMP1_FILE);
        MainActivityHelper.deleteFileIfExists(Constants.STUDENT_AUDIO_TEMP2_FILE);
        MainActivityHelper.deleteFileIfExists(Constants.STUDENT_AUDIO_RAW_PATH);

        // receiver is registered in onResume() method
        mReceiver = new LessonsBroadCastReceiver();

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(CLASS_NAME, "onStart");

        String lessonsJSON = null;

        mSettings = ((EdTech) getApplication()).getSettings();
        mSettings.setDefaultsIfNotSet();

        //mSettings.setFirstTimeLaunch(true);
        if (mSettings.isFirstTimeLaunch()) {
            Toast.makeText(this.getApplicationContext(), getString(R.string.welcome), Toast.LENGTH_SHORT).show();
        }

        if (mSettings.isUseOnlineLessons() && MainActivityHelper.checkInternetConnection(this.getApplicationContext())) {
            // ping the lessons server
            int code = LessonAsyncHttpReader.pingLessons();

            if (code == 200) {
                LessonAsyncHttpReader sh = new LessonAsyncHttpReader();

                // minus is it will freeze process until asynctask is finished, try with a timeout
                try {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        lessonsJSON = sh.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get(2000, TimeUnit.MILLISECONDS);
                    else
                        lessonsJSON = sh.execute().get(2000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this.getApplicationContext(),
                        getString(R.string.could_not_load_online_lessons_text), Toast.LENGTH_SHORT).show();
                // otherwise load the lessons from the application
                lessonsJSON = LessonReader.readDataModel(getApplicationContext());
            }

        } else {
            // check if there is no internet connection and display error message
            if (mSettings.isUseOnlineLessons() && !MainActivityHelper.checkInternetConnection(this.getApplicationContext())) {
                Toast.makeText(this.getApplicationContext(),
                        getString(R.string.could_not_load_online_lessons_text), Toast.LENGTH_SHORT).show();
            }
            // otherwise load the lessons from the application
            lessonsJSON = LessonReader.readDataModel(getApplicationContext());
        }

        Log.d(CLASS_NAME, "Response: > " + lessonsJSON);

        if ((lessonsJSON != null) && (!lessonsJSON.contains("Error"))) {
            mSyllabus = LessonParser.parseLessonModel(lessonsJSON);
            mScores = ((EdTech) getApplication()).getScores();
        } else {
            Toast.makeText(this.getApplicationContext(),
                    getString(R.string.could_not_load_online_lessons_text), Toast.LENGTH_SHORT).show();
        }

    }

    // Unit Selected Listener for Lessons Interface, initiates lesson thread
    public void onUnitSelected(String unit, String title) {

        // The user selected the unit from the LessonsFragment
        // Start processing that information.

        String key = unit + ":" + title;
        int hashcode = 0;

        Log.d(CLASS_NAME, "Key: " + key.hashCode());

        if (isLessonRunning()) {
            hashcode = mAsyncLessonThread.getHashCode();
            Toast.makeText(this.getApplicationContext(),
                    getString(R.string.cancel_lesson_text), Toast.LENGTH_SHORT).show();
            mAsyncLessonThread.cancel(true);
        }

        // check if the user has selected a different lesson and run that new lesson
        // if it is the same lesson, do no execute the lesson, just cancel the current lesson
        if (key.hashCode() != hashcode) {
            mAsyncLessonThread = LessonManagerTask.getInstance(this, mSyllabus, mScores, mProgress);
            mAsyncLessonThread.setHashCode(key.hashCode());
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                mAsyncLessonThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, unit, title, mSettings.isTestingMode());
            else
                mAsyncLessonThread.execute(unit, title, mSettings.isTestingMode());
            Toast.makeText(this.getApplicationContext(),
                    getString(R.string.run_unit_text) + " " + unit + " - " + title, Toast.LENGTH_SHORT).show();
        }

    }

    // Broadcast receiver to accept messages from main lesson thread
    public class LessonsBroadCastReceiver extends BroadcastReceiver {

        private String CLASS_NAME;

        public LessonsBroadCastReceiver() {
            CLASS_NAME = getClass().getName();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT) {
                Bundle extras = intent.getExtras();
                if (extras != null) {

                    // receiver for updates to activity components view pager and flash bar

                    if(extras.containsKey(MessagingConstants.MSG_DATA_KEY_FLASHBAR)) {
                        Log.d(CLASS_NAME, "onReceive " + extras.get(MessagingConstants.MSG_DATA_KEY_FLASHBAR));
                        String status = (String) extras.get(MessagingConstants.MSG_DATA_KEY_FLASHBAR);

                        switch(status) {
                            case MessagingConstants.FLASH_BAR_ON:
                                mFlashBar.setVisibility(View.VISIBLE);
                                //updateScore();
                                break;
                            case MessagingConstants.FLASH_BAR_OFF:
                                mFlashBar.setVisibility(View.GONE);
                                break;
                            default:
                                break;
                        }
                    }

                    if(extras.containsKey(MessagingConstants.MSG_DATA_KEY_SCORE)) {
                        int score = (int) extras.get(MessagingConstants.MSG_DATA_KEY_SCORE);
                        if (mAsyncLessonThread != null) {
                            Log.d(CLASS_NAME, "Score: " + score);
                            mScoreText.setText(getString(R.string.score) + " " + Integer.toString(score));
                        }
                    }

                    if(extras.containsKey(MessagingConstants.MSG_DATA_KEY_COUNTDOWN)) {
                        long delay = (long) extras.get(MessagingConstants.MSG_DATA_KEY_COUNTDOWN);
                        if (mAsyncLessonThread != null) {
                            startCountDownTimer(delay);
                        }
                    }

                    if(extras.containsKey(MessagingConstants.MSG_DATA_KEY_VIEW)) {
                        Log.d(CLASS_NAME, "onReceive " + extras.get(MessagingConstants.MSG_DATA_KEY_VIEW));
                        String subject = (String) extras.get(MessagingConstants.MSG_DATA_KEY_VIEW);
                        String lesson = (String) extras.get(MessagingConstants.MSG_DATA_KEY_LESSON);
                        String phonetic = (String) extras.get(MessagingConstants.MSG_DATA_KEY_PHONETIC);
                        int item;

                        LessonsFragmentTab frag0 =
                                (LessonsFragmentTab) getSupportFragmentManager().
                                        findFragmentByTag("android:switcher:" + R.id.pager + ":0");
                        AbcFragmentTab frag1 =
                                (AbcFragmentTab) getSupportFragmentManager().
                                        findFragmentByTag("android:switcher:" + R.id.pager + ":1");
                        MathFragmentTab frag2 =
                                (MathFragmentTab) getSupportFragmentManager().
                                        findFragmentByTag("android:switcher:" + R.id.pager + ":2");
                        SpeakFragmentTab frag3 =
                                (SpeakFragmentTab) getSupportFragmentManager().
                                        findFragmentByTag("android:switcher:" + R.id.pager + ":3");

                        if (frag1 != null) frag1.clearExercise();
                        if (frag2 != null) frag2.clearExercise();
                        if (frag3 != null) frag3.clearExercise();

                        switch(subject){
                            case MessagingConstants.FRAGMENT_TAG_LESSONS_LIST:
                                // needed in this order to avoid issues with cached object
                                mViewPager.setCurrentItem(0);
                                frag0.refresh();
                                break;
                            case MessagingConstants.FRAGMENT_TAG_WRITING:
                                Log.d(CLASS_NAME, "Create fragment writing.");
                                Log.d(CLASS_NAME, " Lesson: " + lesson);
                                Log.d(CLASS_NAME, " Phonetic: " + phonetic);
                                // needed in this order to avoid issues with cached object
                                mViewPager.setCurrentItem(1);
                                if(frag1 == null) {
                                    item = mViewPager.getCurrentItem();
                                    frag1 = (AbcFragmentTab) getSupportFragmentManager().
                                            findFragmentByTag("android:switcher:" + R.id.pager + ":" + item);
                                }
                                frag1.setExercise(lesson, phonetic);
                                break;
                            case MessagingConstants.FRAGMENT_TAG_MATH:
                                Log.d(CLASS_NAME, "Create fragment math.");
                                Log.d(CLASS_NAME, " Lesson: " + lesson);
                                // needed in this order to avoid issues with cached object
                                mViewPager.setCurrentItem(2);
                                if(frag2 == null) {
                                    item = mViewPager.getCurrentItem();
                                    frag2 = (MathFragmentTab) getSupportFragmentManager().
                                            findFragmentByTag("android:switcher:" + R.id.pager + ":" + item);
                                }
                                frag2.setExercise(lesson, phonetic);
                                break;
                            case MessagingConstants.FRAGMENT_TAG_READING:
                                Log.d(CLASS_NAME, "Create fragment reading.");
                                Log.d(CLASS_NAME, " Lesson: " + lesson);
                                // needed in this order to avoid issues with cached object
                                mViewPager.setCurrentItem(3);
                                if (frag3 == null) {
                                    item = mViewPager.getCurrentItem();
                                    frag3 = (SpeakFragmentTab) getSupportFragmentManager().
                                            findFragmentByTag("android:switcher:" + R.id.pager + ":" + item);
                                }
                                frag3.setExercise(lesson, phonetic);
                                break;
                            case MessagingConstants.TAG_CANCEL:
                                String clear = (String) extras.get(MessagingConstants.MSG_DATA_KEY_CLEAR);
                                switch(clear) {
                                    case MessagingConstants.FRAGMENT_TAG_WRITING:
                                        if(frag1 != null) frag1.clearExercise();
                                        break;
                                    case MessagingConstants.FRAGMENT_TAG_MATH:
                                        if (frag2 != null) frag2.clearExercise();
                                        break;
                                    case MessagingConstants.FRAGMENT_TAG_READING:
                                        if (frag3 != null) frag3.clearExercise();
                                        break;
                                    default:
                                        break;
                                }
                                frag0.refresh();
                                break;
                            default:
                                Log.d(CLASS_NAME, "Invalid fragment do nothing.");
                                Log.d(CLASS_NAME, " Lesson: " + lesson);
                                break;
                        }

                    }
                }
            }
        }
    }


    // Text-To-Speech initialization
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "Request Code: " + requestCode);
        switch(requestCode) {
            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //user has the necessary data - create the TTS
                    ttsObj = new TextToSpeech(getApplicationContext(), this);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
                break;
            default:
                break;

        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {

            // TODO: add in infrastructure to use ENGLISH local if isEnglish or isSwahilli
            // TODO: use CHINESE and FRENCH locals when that is supported in the future
            // TODO: use default local as back up or if not a supported language

            ttsObj.setSpeechRate((float) 0.75);
            if (ttsObj.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
                ttsObj.setLanguage(Locale.ENGLISH);
            }
            else {
                Locale current = getResources().getConfiguration().locale;
                ttsObj.setLanguage(current);
            }
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(getApplicationContext(), getString(R.string.text_to_speech_fail_text), Toast.LENGTH_LONG).show();
        }
    }

    // Base Interface
    public Settings getSettings() {
        return mSettings;
    }

    public Scores getScores() {
        return mScores;
    }

    public LessonManagerTask getLessonThread() {
        return mAsyncLessonThread;
    }

    public boolean isLessonRunning() {
        boolean lessonThreadRunning =
                (mAsyncLessonThread != null) &&
                        (mAsyncLessonThread.getStatus() == AsyncTask.Status.RUNNING);
        return lessonThreadRunning;
    }

    // Slate Interactions
    public TextToSpeech getTextToSpeech() {
        return ttsObj;
    }

    public void speakText(String text, boolean toFile) {

        if (ttsObj != null) {

            MainActivityHelper.speak(ttsObj, text);

            if (toFile == true) {
                MainActivityHelper.synthesizeToFile(ttsObj, text);
            }
        }
    }

    public void speakPhonetic(String text, String phonetic, boolean toFile) {
        Log.d(CLASS_NAME, "speakPhonetic - Test: " + text + " Phonetic: " + phonetic);
        if (ttsObj != null) {

            //Log.d(CLASS_NAME, "Default Engine: " + ttsObj.getDefaultEngine());
            //Log.d(CLASS_NAME, "Available Engines: " + ttsObj.getEngines());
            // TODO: need to formulate xsampa or ipa SSML markup with text and phonetic
            // TODO: the SSML solution is not working ...
            // TODO: using just the phonetic has some interesting side effects.
            // TODO: could just sound out for Swahili and use regular text for English and other supported languages
            String ssml = phonetic;
            //String ssml = "<speak xml:lang=\"en-US\"> <phoneme alphabet=\"xsampa\" ph=\"faIv\"/>.</speak>";
            //String ssml = "<speak xml:lang=\"en-US\"> <phoneme alphabet=\"xsampa\" ph=\""+phonetic+"\"/>"+text+"</speak>";
            //String ssml = "<speak xml:lang=\"en-US\"> <phoneme alphabet=\"xsampa\" ph=\""+phonetic+"\"/>"+phonetic+"</speak>";
            //String ssml = "<speak xml:lang=\"en-US\"> <phoneme alphabet=\"xsampa\" ph=\""+phonetic+"\"/>.</speak>";

            MainActivityHelper.speak(ttsObj, ssml);

            if (toFile == true) {
                MainActivityHelper.synthesizeToFile(ttsObj, ssml);
            }
        }
    }

    public void animateSticker(Constants.StickerType type) {
        Log.d(CLASS_NAME, "animateSticker");

        switch(type) {
            case IMAGE:
                MainActivityHelper.setImage(mStickerView);
                break;
            case AVATAR:
                MainActivityHelper.setAvatar(mStickerView);
                break;
            default:
                MainActivityHelper.setImage(mStickerView);
                break;
        }

        // Type casting the Animation drawable
        mStickerView.setVisibility(View.VISIBLE);
        mStickerFrameAnimation = (AnimationDrawable) mStickerView.getBackground();

        //set true if you want to animate only once
        mStickerFrameAnimation.setOneShot(true);
        mStickerFrameAnimation.start();

    }
    public void clearSticker() {
        if (mStickerFrameAnimation != null) {
            mStickerFrameAnimation.stop();
            mStickerView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_profile:
                // cancel the current lesson thread
                if (isLessonRunning()) {
                    Toast.makeText(this.getApplicationContext(),
                            getString(R.string.cancel_lesson_text), Toast.LENGTH_SHORT).show();
                    mAsyncLessonThread.cancel(true);
                }
                // start the settings activity
                Intent settingsIntent = new Intent(getApplication(), ProfileActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onResume() {
        super.onResume();

        // Obtain the shared Tracker instance.
        Tracker tracker = ((EdTech) getApplication()).getDefaultTracker();
        tracker.setScreenName(Constants.VIEW_SCREEN + getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        //mNightModeHelper.startSensing();
        registerReceiver(mReceiver, new IntentFilter(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT));
    }

    @Override
    public void onPause(){
        super.onPause();
        //mNightModeHelper.stopSensing();

        if (mAsyncLessonThread != null) {
            mAsyncLessonThread.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(CLASS_NAME, "onStop");

        if (mSettings.isFirstTimeLaunch()) {
            mSettings.setFirstTimeLaunch(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(ttsObj != null) {
            ttsObj.stop();
            ttsObj.shutdown();
        }

        if (mReceiver != null ) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextButton:
                if (mAsyncLessonThread != null) {
                    mAsyncLessonThread.sendMessage(MessagingConstants.MSG_POKE_THREAD);
                }
                break;
            default:
                break;
        }
    }

    public void startCountDownTimer(long delay) {

        // check if another timer is running and cancel it.
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                String display = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
                mCounterText.setText(display);
            }

            @Override
            public void onFinish() {
                mCounterText.setText("00:00");
            }
        }.start();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new LessonsFragmentTab().newInstance(mSyllabus);
                case 1:
                    return new AbcFragmentTab();
                case 2:
                    return new MathFragmentTab();
                case 3:
                    return new SpeakFragmentTab();
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                default:
                    break;
            }
            return null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppPermissions.PERMISSIONS_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                        //&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // related task you need to do.
                    //for (int i = 0; i < permissions.length; i++) {
                        //Toast.makeText(this.getApplicationContext(), permissions[i] +  " : " + grantResults[i], Toast.LENGTH_SHORT).show();
                    //}
                    Toast.makeText(this.getApplicationContext(), getString(R.string.thank_you), Toast.LENGTH_LONG).show();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this.getApplicationContext(), getString(R.string.missing_permissions_needed), Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
