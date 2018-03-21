package com.edtech;

import android.media.AudioFormat;
import android.os.Environment;

/**
 * Created by gautamkarnik on 2015-09-21.
 */
public class Constants {

    public static final String EDTECH_FOLDER = Environment.getExternalStorageDirectory()
            + "/Documents/EdTech";

    public static final String TTS_AUDIO_FILE_PATH = Constants.EDTECH_FOLDER + "/textRecord.wav";
    public static final String STUDENT_AUDIO_FILE_PATH = Constants.EDTECH_FOLDER + "/studentRecord.wav";
    public static final String LESSONS_FILE_PATH = Constants.EDTECH_FOLDER + "/lessons.json";

    // old files we need to check for and delete
    public static final String TTS_AUDIO_TEMP1_FILE = Constants.EDTECH_FOLDER +"/temp1.wav";
    public static final String STUDENT_AUDIO_TEMP2_FILE = Constants.EDTECH_FOLDER +"/temp2.wav";
    public static final String STUDENT_AUDIO_RAW_PATH = Constants.EDTECH_FOLDER + "/studentRaw.tmp";

    public static final int AUDIO_RECORDER_TIMER_INTERVAL = 100;
    public static final int AUDIO_RECORDER_BPS = 16;
    public static final int AUDIO_RECORDER_NUM_CHANNELS = 1;
    public static final int AUDIO_RECORDER_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_RECORDER_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public static final int AUDIO_PLAYER_CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_RESAMPLE_RATE = 8000;

    public static final int AVATAR_LION = 0;
    public static final int AVATAR_ELEPHANT = 1;
    public static final int AVATAR_HORSE = 2;
    public static final int AVATAR_GOAT = 3;

    public static final int IMAGE_GREEN_FIREWORK = 0;
    public static final int IMAGE_RED_FIREWORK = 1;
    public static final int IMAGE_BLUE_FIREWORK = 2;

    public enum StickerType {AVATAR, IMAGE};

    // Google Analytic Event
    public static final String CATEGORY_ACHIEVEMENT = "Achievement";
    public static final String CATEGORY_CANCEL = "Cancel";
    public static final String CATEGORY_SELECTED = "Selected";

    public static final String ACTION_TEST_COMPLETED = "Test Complete";
    public static final String ACTION_LESSON_COMPLETED = "Lesson Complete";
    public static final String ACTION_TEST_ABORTED = "Test Aborted";
    public static final String ACTION_LESSON_ABORTED = "Lesson Aborted";
    public static final String ACTION_TEST_STARTED = "Test Started";
    public static final String ACTION_LESSON_STARTED = "Lesson Started";

    // Google Analytic Srcreen
    public static final String VIEW_SCREEN = "Screen~";
    public static final String VIEW_TAB = "Tab~";
}
