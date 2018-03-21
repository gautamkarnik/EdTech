package com.edtech.fragments;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;

/**
 * Created by gautamkarnik on 2015-05-14.
 */
public abstract class BaseFragmentTab extends Fragment {

    private final String CLASS_NAME = getClass().getSimpleName();
    protected static final String LESSON_KEY = "lesson_key";
    protected static final String PHONEITC_KEY = "phonetic_key";

    public abstract void setExercise(String lesson, String phonetic);
    public abstract void clearExercise();

    public SlateInteractions mCallback;

    protected boolean toolTipGuard = true;

    // Container Activity must implement this interface
    public interface SlateInteractions extends BaseInterface {
        public void speakText(String text, boolean toFile);
        public void speakPhonetic(String text, String phonetic, boolean toFile);
        public TextToSpeech getTextToSpeech();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface.  if not, it throws an exception.
        try {
            mCallback = (SlateInteractions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException((activity.toString() +
                    " must implement BaseFragment SlateInteractions"));
        }
    }
}
