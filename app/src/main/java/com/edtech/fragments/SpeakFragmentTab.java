package com.edtech.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.edtech.AppPermissions;
import com.edtech.Constants;
import com.edtech.EdTech;
import com.edtech.R;
import com.edtech.activities.MessagingConstants;
import com.edtech.audio.MicrophoneAudioPlayer;
import com.edtech.audio.PolarView;
import com.edtech.audio.VisualizerView;
import com.edtech.services.MicrophoneService;
import com.edtech.utilities.ToolTipGenerator;

import it.sephiroth.android.library.tooltip.Tooltip;

public class SpeakFragmentTab extends BaseFragmentTab implements View.OnClickListener  {
    private final String CLASS_NAME = getClass().getSimpleName();

    protected String exerciseToDisplay="";
    protected String phoneticToPronouce="";

    private TextView eTextView;
    private ImageButton clearButton;
    private ImageButton speakTextBtn; // for text to speech
    private ImageButton readTextBtn; // for voice comparison when reading
    private boolean readPressed = false;

    private VisualizerView audioView;
    private PolarView polarView;
    private MicrophoneService microphoneService;

    public SpeakFragmentTab() {

    }

    public static SpeakFragmentTab newInstance(String lesson, String phonetic) {
        SpeakFragmentTab fragment = new SpeakFragmentTab();
        Bundle bundle = new Bundle();
        bundle.putSerializable(LESSON_KEY, lesson);
        bundle.putSerializable(PHONEITC_KEY, phonetic);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setExercise(String lesson, String phonetic) {
        // putting in the check for null due to some issues with the cached fragment
        // when changing orientations

        if ((eTextView != null)) {
            exerciseToDisplay = lesson;
            phoneticToPronouce = phonetic;
            eTextView.setText(exerciseToDisplay);
            mCallback.clearSticker();

            // don't want to sythesize to file if there is an empty lesson
            if (exerciseToDisplay.isEmpty()) {
                return;
            }

            if (mCallback.getSettings() != null) {
                if (mCallback.getSettings().isSwahiliOn()) {
                    mCallback.speakPhonetic(exerciseToDisplay, phoneticToPronouce, true);
                } else {
                    mCallback.speakText(exerciseToDisplay, true);
                }
            }

        }
    }

    @Override
    public void clearExercise() {
        // putting in the check for null due to some issues with the cached fragment
        // when changing orientations

        if ((eTextView != null)) {
            exerciseToDisplay = "";
            phoneticToPronouce = "";
            eTextView.setText(exerciseToDisplay);
            mCallback.clearSticker();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve the syllabus object using the get/setArguments() interface
        Bundle arguments = getArguments();
        if (arguments != null) {
            exerciseToDisplay = arguments.getString(LESSON_KEY);
            phoneticToPronouce = arguments.getString(PHONEITC_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_speak, container, false);
        clearButton = (ImageButton) rootView.findViewById(R.id.clearSpeech);
        clearButton.setOnClickListener(this);

        speakTextBtn = (ImageButton) rootView.findViewById(R.id.speakText);
        speakTextBtn.setOnClickListener(this);

        readTextBtn = (ImageButton) rootView.findViewById(R.id.readText);
        readTextBtn.setOnClickListener(this);
        readPressed = false;

        eTextView = (TextView) rootView.findViewById(R.id.exerciseSpeech);

        //set up the audio visualizer objects
        audioView = (VisualizerView) rootView.findViewById(R.id.VisualizerView);
        polarView = (PolarView) rootView.findViewById(R.id.PolarView);

        setExercise(exerciseToDisplay, phoneticToPronouce);

        return rootView;
    }

    @Override
    public void onClick(View view) {

        // clear the sticker on any button press since this is still a work in progress
        mCallback.clearSticker();

        switch (view.getId()) {
            case R.id.clearSpeech:
                //eTextView.setText("");
                audioView.init();
                audioView.invalidate();
                polarView.init();
                polarView.invalidate();

                if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                    if (mCallback.isLessonRunning()) {
                        ToolTipGenerator.showTooltip(this.getContext(), readTextBtn, getString(R.string.record_your_voice), Tooltip.Gravity.BOTTOM);
                    }
                    toolTipGuard = false;
                }

                break;
            case R.id.speakText:
                if (!readPressed) { // make sure you can't record a playback
                    if (mCallback.isLessonRunning()) {
                        // play back the tts recording if a lesson is running
                        if (mCallback.getSettings().isSwahiliOn()) {
                            mCallback.speakPhonetic(exerciseToDisplay, phoneticToPronouce, true);
                        } else {
                            mCallback.speakText(exerciseToDisplay, true);
                        }
                    } else {
                        // play back the student recording if a lesson is not running
                        MicrophoneAudioPlayer audioPlayer = new MicrophoneAudioPlayer();
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                            audioPlayer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            audioPlayer.execute();
                    }

                    if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                        ToolTipGenerator.showTooltip(this.getContext(), clearButton, getString(R.string.clear_voice_slate), Tooltip.Gravity.BOTTOM);
                    }
                }
                break;
            case R.id.readText:
                // change the color of the visualizer here
                // start recording wav file for comparison
                // compare wav files and get score

                if (microphoneService.isRunning()) {
                    readPressed = !readPressed;
                    if (readPressed) {
                        // start recording, prompt user for next action with toggled image button
                        readTextBtn.setImageResource(R.drawable.ic_action_stop_recording);
                        audioView.setCycleColor(true);
                        polarView.setCycleColor(true);
                        microphoneService.startRecording();

                        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                            ToolTipGenerator.showTooltip(this.getContext(), readTextBtn, getString(R.string.stop_recording), Tooltip.Gravity.BOTTOM);
                        }
                    } else {
                        // stop recording, prompt user for next action with toggled image button
                        microphoneService.stopRecording();
                        polarView.setCycleColor(false);
                        audioView.setCycleColor(false);
                        readTextBtn.setImageResource(R.drawable.ic_action_start_recording);
                        compareVoices();

                        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                            ToolTipGenerator.showTooltip(this.getContext(), speakTextBtn, getString(R.string.listen), Tooltip.Gravity.BOTTOM);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AppPermissions appPermissions = new AppPermissions(this.getActivity());
        if(!appPermissions.checkPermissionForRecordAudio() || !appPermissions.checkPermissionForWriteExternalStorage()) {
            Toast.makeText(this.getContext(), this.getContext().getString(R.string.missing_permissions_needed), Toast.LENGTH_LONG).show();
        }

        microphoneService = ((EdTech) getActivity().getApplication()).getMicrophoneService();
        if (microphoneService.isRunning()) {
            microphoneService.startMicrophoneAudio(audioView, polarView);

            if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
               ToolTipGenerator.showTooltip(this.getContext(), readTextBtn, getString(R.string.record_your_voice), Tooltip.Gravity.BOTTOM);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        microphoneService = ((EdTech) getActivity().getApplication()).getMicrophoneService();
        if (microphoneService.isRunning()) {
            microphoneService.stopMicrophoneAudio();  // this will also close the recording
            readPressed = false;
        }
    }

    private void compareVoices() {
        if (mCallback.isLessonRunning() && !exerciseToDisplay.equals("") && microphoneService.isRunning()) {
            float sim = microphoneService.compareVoiceFiles();
            Log.d(CLASS_NAME, "Similarity: " + String.format("%3.10f", sim));

            if (sim > 0) {
                Toast.makeText(getActivity(), getString(R.string.match), Toast.LENGTH_SHORT).show();
                mCallback.animateSticker(Constants.StickerType.IMAGE);
                if (mCallback.getLessonThread() != null) {
                    mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_POKE_THREAD);
                    if ((mCallback.getSettings().isTestingMode())) {
                        mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_INCREMENT_SCORE_5);
                    }
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
            }
        }
    }
}