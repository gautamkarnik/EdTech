package com.edtech.activities;

/**
 * Created by gautamkarnik on 2015-06-06.
 */

/**
 * Defines several constants used between LessonManagerTask and UI.
 */
public interface MessagingConstants {

    // used in the Broadcast receiver
    public static final String KEY_INTENT_CHANGE_FRAGMENT = "CHANGE_FRAGMENT";

    public static final String FLASH_BAR_ON = "on";
    public static final String FLASH_BAR_OFF = "off";
    public static final String TAG_CANCEL = "cancel";

    public static final String FRAGMENT_TAG_READING = "reading";
    public static final String FRAGMENT_TAG_WRITING = "writing";
    public static final String FRAGMENT_TAG_MATH = "math";
    public static final String FRAGMENT_TAG_LESSONS_LIST = "list";


    //used in the LessonManagerTask Asychronous thread
    public static final String MSG_DATA_KEY = "data";
    public static final String MSG_DATA_KEY_VIEW = "view";
    public static final String MSG_DATA_KEY_LESSON = "lesson";
    public static final String MSG_DATA_KEY_PHONETIC = "phonetic";
    public static final String MSG_DATA_KEY_FLASHBAR = "flashbar";
    public static final String MSG_DATA_KEY_CLEAR = "clear";
    public static final String MSG_DATA_KEY_SCORE = "score";
    public static final String MSG_DATA_KEY_COUNTDOWN = "countdown";

    public static final int MSG_CLEAR_SCORE = 1;
    public static final int MSG_INCREMENT_SCORE_5 = 2;
    public static final int MSG_INCREMENT_SCORE_10 = 3;
    public static final int MSG_CANCEL_THREAD = 4;
    public static final int MSG_POKE_THREAD = 5;
}
