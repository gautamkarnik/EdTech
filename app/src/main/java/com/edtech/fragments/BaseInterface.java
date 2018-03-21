package com.edtech.fragments;

import com.edtech.Constants;
import com.edtech.activities.LessonManagerTask;
import com.edtech.application.Scores;
import com.edtech.application.Settings;

/**
 * Created by gautamkarnik on 2015-06-02.
 */
public interface BaseInterface {
    public Settings getSettings();
    public Scores getScores();
    public LessonManagerTask getLessonThread();
    public boolean isLessonRunning();
    public void animateSticker(Constants.StickerType type);
    public void clearSticker();
}
