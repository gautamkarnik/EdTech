package com.edtech.fragments;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.edtech.Constants;
import com.edtech.R;
import com.edtech.activities.MessagingConstants;
import com.edtech.animations.DrawBlob;
import com.edtech.animations.DrawText;
import com.edtech.animations.MathPanel;
import com.edtech.math.RPN;
import com.edtech.math.ShuntingYard;
import com.edtech.utilities.ToolTipGenerator;

import java.util.ArrayList;
import java.util.regex.Pattern;

import it.sephiroth.android.library.tooltip.Tooltip;

public class MathFragmentTab extends BaseFragmentTab implements GestureOverlayView.OnGesturePerformedListener, View.OnClickListener {

    private final String CLASS_NAME = getClass().getSimpleName();

    protected String txtToDisplay="";
    protected String exerciseToDisplay="";
    protected String phoneticToPronouce="";

    protected GestureLibrary gLibrary;
    private TextView gTextView, eTextView;
    private ImageButton clearButton, speakButton, mathButton;

    private MathPanel mathPanel;
    private DrawText drawText;
    private DrawBlob drawBlob;

    private boolean isFirstTimeGesture = true;

    public MathFragmentTab() {

    }

    public static MathFragmentTab newInstance(String lesson, String phonetic) {
        MathFragmentTab fragment = new MathFragmentTab();
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

        if ((eTextView != null) && (drawText != null) && (gTextView != null)) {
            exerciseToDisplay = lesson;
            phoneticToPronouce = phonetic;
            eTextView.setText(exerciseToDisplay);

            if (mCallback.getSettings() != null) {
                if (mCallback.getSettings().isSwahiliOn()) {
                    mCallback.speakPhonetic(exerciseToDisplay, phonetic, false);
                } else {
                    mCallback.speakText(replaceEquationWithSymbols(exerciseToDisplay), false);
                }
            }

            clearSlate();
        }
    }

    @Override
    public void clearExercise() {

        // putting in the check for null due to some issues with the cached fragment
        // when changing orientations

        if ((eTextView != null) && (drawText != null) && (gTextView != null)) {
            exerciseToDisplay = "";
            phoneticToPronouce = "";
            eTextView.setText(exerciseToDisplay);

            clearSlate();
        }
    }

    public void clearSlate() {
        drawText.setText(" ", Color.WHITE);

        txtToDisplay = "";
        gTextView.setText(txtToDisplay);

        mCallback.clearSticker();
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
        View rootView = inflater.inflate(R.layout.fragment_123, container, false);
        clearButton = (ImageButton) rootView.findViewById(R.id.clear123);
        clearButton.setOnClickListener(this);
        speakButton = (ImageButton) rootView.findViewById(R.id.speak123);
        speakButton.setOnClickListener(this);
        mathButton  = (ImageButton) rootView.findViewById(R.id.math123);
        mathButton.setOnClickListener(this);
        gTextView = (TextView) rootView.findViewById(R.id.text123);
        gTextView.addTextChangedListener(textWatcher);
        eTextView = (TextView) rootView.findViewById(R.id.exercise123);

        gLibrary = GestureLibraries.fromRawResource(rootView.getContext(), R.raw.gestures123);
        if (gLibrary != null) {
            if (!gLibrary.load()) {
                Log.e(CLASS_NAME, "Gesture library was not loaded.");
                //finish();
            } else {
                GestureOverlayView gView = (GestureOverlayView) rootView.findViewById(R.id.gestureView123);
                gView.addOnGesturePerformedListener(this);
            }
        }

        Bitmap blob = BitmapFactory.decodeResource(getResources(), R.raw.chalk);
        drawBlob = new DrawBlob(rootView.getContext(), blob);
        drawText = new DrawText(rootView.getContext());

        mathPanel = (MathPanel) rootView.findViewById(R.id.MathPanel);
        mathPanel.setDrawBlob(drawBlob);
        mathPanel.setDrawText(drawText);

        setExercise(exerciseToDisplay, phoneticToPronouce);
        return rootView;
    }

    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture){
        ArrayList<Prediction> predictions = gLibrary.recognize(gesture);
        //Log.d(CLASS_NAME, predictions.toString());
        //for (int i = 0; i < predictions.size(); i++) {
        //    Log.d(CLASS_NAME, String.valueOf(predictions.get(i).score));
        //}

        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard && isFirstTimeGesture) {
            ToolTipGenerator.showTooltip(getContext(), mathPanel, getString(R.string.write_on_the_slate), Tooltip.Gravity.CENTER);
        }

        int currPos = txtToDisplay.length();
        int correctAnswer = evaluateExpression(exerciseToDisplay);
        String answer = Integer.toString(correctAnswer);

        if (mCallback.isLessonRunning() && (currPos < answer.length()) && (!exerciseToDisplay.equals(""))) {
            for (int j = 0; j < predictions.size(); j++) {
                String prediction = String.valueOf(predictions.get(j).name);
                String target = String.valueOf(answer.charAt(currPos));

                if (predictions.get(j).score > 1.5) {
                    if (prediction.equals(target)) {
                        txtToDisplay += prediction;
                        gTextView.setText(txtToDisplay);
                        break;
                    }
                }
                //Log.d(CLASS_NAME, String.valueOf(predictions.get(j).score));
            }
        } else {
            if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
                String val = predictions.get(0).name;
                switch(val) {
                    case "+":
                    case "-":
                    case "÷":
                    case "×":
                    case "^":
                    case "(":
                    case ")":
                        txtToDisplay += " ";
                        txtToDisplay += predictions.get(0).name;
                        txtToDisplay += " ";
                        break;
                    default:
                        txtToDisplay += predictions.get(0).name;
                        break;
                }

                gTextView.setText(txtToDisplay);
            }
        }

        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard && isFirstTimeGesture) {
            ToolTipGenerator.showTooltip(this.getContext(), speakButton, getString(R.string.hear_the_slate), Tooltip.Gravity.BOTTOM);
            isFirstTimeGesture = false;
        }
    }

    private int evaluateExpression(String infix) {
        String postfix = ShuntingYard.infixToPostfix(infix);
        int value = (int) RPN.evalRPN(postfix);
        return value;
    }

    private String replaceEquationWithSymbols(String text) {
        // NOTE: TTS does not say some math symbols properly so have to do find and replace
        String newText = text;
        newText = newText.replaceAll(Pattern.quote("+"), getString(R.string.plus));
        newText = newText.replaceAll(Pattern.quote("-"), getString(R.string.minus));
        newText = newText.replaceAll(Pattern.quote("÷"), getString(R.string.divided_by));
        newText = newText.replaceAll(Pattern.quote("×"), getString(R.string.times));
        newText = newText.replaceAll(Pattern.quote("^"), getString(R.string.raised_to));
        return newText;
    }

    public void drawMath(String value) {
        drawText.setText(value, Color.WHITE);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mCallback.isLessonRunning() && mCallback.getSettings().isLearningMode()) {
            mathButton.setVisibility(View.VISIBLE);
        } else {
            mathButton.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear123:
                clearSlate();

                if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                    if (mCallback.isLessonRunning() && mCallback.getSettings().isLearningMode()) {
                        ToolTipGenerator.showTooltip(this.getContext(), mathButton, getString(R.string.see_and_hear_the_answer), Tooltip.Gravity.BOTTOM);
                    }
                    toolTipGuard = false;
                }

                break;
            case R.id.speak123:
                if (mCallback.isLessonRunning() && !exerciseToDisplay.equals("")) {
                    mCallback.clearSticker();

                    if (mCallback.getSettings().isSwahiliOn()) {
                        mCallback.speakPhonetic(exerciseToDisplay, phoneticToPronouce, false);
                    } else {
                        mCallback.speakText(replaceEquationWithSymbols(exerciseToDisplay), false);
                    }

                    if ((mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard)) {
                        if (mCallback.getSettings().isLearningMode()) {
                            ToolTipGenerator.showTooltip(this.getContext(), mathButton, getString(R.string.see_and_hear_the_answer), Tooltip.Gravity.BOTTOM);
                        } else {
                            ToolTipGenerator.showTooltip(this.getContext(), clearButton, getString(R.string.clear_the_slate), Tooltip.Gravity.BOTTOM);
                        }
                    }
                } else {
                    mCallback.speakText(replaceEquationWithSymbols(txtToDisplay), false);

                    if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                        ToolTipGenerator.showTooltip(this.getContext(), clearButton, getString(R.string.clear_the_slate), Tooltip.Gravity.BOTTOM);
                    }
                }

                break;
            case R.id.math123:
                int value;
                if (mCallback.isLessonRunning() && !exerciseToDisplay.equals("")) {
                    mCallback.clearSticker();
                    if(mCallback.getSettings().isLearningMode()) {
                        value = evaluateExpression(exerciseToDisplay);
                        String answerToSpeak = String.valueOf(value);
                        mCallback.speakText(answerToSpeak, false);
                        drawMath(answerToSpeak);
                    }
                }
                else {
                    // keep this around in case someone tries to do math during a lesson on another slate
                    value = evaluateExpression(txtToDisplay);
                    txtToDisplay = String.valueOf(value);
                    gTextView.setText(txtToDisplay);
                    mCallback.speakText(txtToDisplay, false);
                }

                if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                    ToolTipGenerator.showTooltip(this.getContext(), clearButton, getString(R.string.clear_the_slate), Tooltip.Gravity.BOTTOM);
                }

                break;
            default:
                break;
        }
    }

    // TextViewWatcher Implementation
    private final TextWatcher textWatcher = new TextWatcher() {

        public Integer tryParse(String text) {
            try {
                return new Integer(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = (String) s.toString();
            //Log.d(CLASS_NAME, "Comparing text: " + text);

            // checking if there is an expression to evaluate in the exercise
            if(!exerciseToDisplay.equals("") && !text.equals("")
                    && mCallback.isLessonRunning()) {
                int correctAnswer = evaluateExpression(exerciseToDisplay);
                Integer answer = tryParse(text);
                if (answer != null) {
                    if(correctAnswer == answer) {
                        // this is where we can increment higher score for lessons
                        Toast.makeText(getActivity(), getString(R.string.correct), Toast.LENGTH_SHORT).show();
                        mCallback.animateSticker(Constants.StickerType.AVATAR);
                        if (mCallback.getLessonThread() != null) {
                            if(mCallback.getSettings().isTestingMode()) {
                                mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_INCREMENT_SCORE_5);
                            }
                            mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_POKE_THREAD);
                        }
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}
