package com.edtech.fragments;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.edtech.Constants;
import com.edtech.R;
import com.edtech.activities.MainActivityHelper;
import com.edtech.activities.MessagingConstants;
import com.edtech.animations.AnimationPanel;
import com.edtech.animations.DrawBlob;
import com.edtech.animations.DrawText;
import com.edtech.utilities.ToolTipGenerator;

import java.util.ArrayList;

import it.sephiroth.android.library.tooltip.Tooltip;

public class AbcFragmentTab extends BaseFragmentTab implements
        GestureOverlayView.OnGesturePerformedListener, View.OnClickListener {

    private final String CLASS_NAME = getClass().getSimpleName();

    protected String txtToDisplay="";
    protected String exerciseToDisplay="";
    protected String phoneticToPronouce="";
    protected GestureLibrary gLibrary;

    private TextView gTextView, eTextView;
    private ImageButton clearButton, speakButton;

    private AnimationPanel animationPanel;
    private DrawText drawText;
    private DrawBlob drawBlob;

    private WebView webView;

    private boolean isFirstTimeGesture = true;

    public AbcFragmentTab() {

    }

    public static AbcFragmentTab newInstance(String lesson, String phonetic) {
        AbcFragmentTab fragment = new AbcFragmentTab();
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

            if ((mCallback.getSettings() != null)) {
                if (mCallback.getSettings().isSwahiliOn()) {
                    mCallback.speakPhonetic(exerciseToDisplay, phonetic, false);
                } else {
                    mCallback.speakText(exerciseToDisplay, false);
                }

                if(mCallback.getSettings().isLearningMode()) {
                    drawText.setText(exerciseToDisplay, Color.WHITE);
                }
            }

            txtToDisplay = "";
            gTextView.setText(txtToDisplay);
            mCallback.clearSticker();

            if (!exerciseToDisplay.equals("") && MainActivityHelper.checkInternetConnection(this.getActivity().getApplicationContext())) {
                webView.setVisibility(View.VISIBLE);
                webView.setEnabled(true);
                String lang;
                if (mCallback.getSettings().isEnglishOn()) {
                    lang = "&lr=lang_en";
                } else {
                    lang = "&lr=lang_sw";
                }
                // can use as_rights here but normal search gives better results
                // &as_rights=cc_sharealike, cc_publicdomain or cc_attribute
                String url = "http://www.google.com/search?safe=on&site=imghp&tbm=isch&as_rights=cc_sharealike" + lang + "&q=";

                webView.loadUrl(url + exerciseToDisplay);
            }
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
        webView.setEnabled(false);
        webView.setVisibility(View.GONE);

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
        View rootView = inflater.inflate(R.layout.fragment_abc, container, false);

        clearButton = (ImageButton) rootView.findViewById(R.id.clearAbc);
        clearButton.setOnClickListener(this);

        speakButton = (ImageButton) rootView.findViewById(R.id.speakAbc);
        speakButton.setOnClickListener(this);

        gTextView = (TextView) rootView.findViewById(R.id.textAbc);
        gTextView.addTextChangedListener(txtWatcher);

        eTextView = (TextView) rootView.findViewById(R.id.exerciseAbc);

        gLibrary = GestureLibraries.fromRawResource(rootView.getContext(), R.raw.gestureslc);
        if (gLibrary != null) {
            if (!gLibrary.load()) {
                Log.e(CLASS_NAME, "Gesture library was not loaded.");
                //finish();
            } else {
                GestureOverlayView gView = (GestureOverlayView) rootView.findViewById(R.id.gestureViewAbc);
                gView.addOnGesturePerformedListener(this);
            }
        }

        Bitmap blob = BitmapFactory.decodeResource(getResources(), R.raw.chalk);
        drawBlob = new DrawBlob(rootView.getContext(), blob);
        drawText = new DrawText(rootView.getContext());

        animationPanel = (AnimationPanel) rootView.findViewById(R.id.AnimationPanel);
        animationPanel.setDrawBlob(drawBlob);
        animationPanel.setDrawText(drawText);

        webView = (WebView) rootView.findViewById(R.id.webViewAbc);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        Paint webPaint = new Paint();
        webPaint.setAlpha(128);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, webPaint);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Log.d(CLASS_NAME, "onPageFinished");
                super.onPageFinished(view, url);
                if (url.contains("google")) {
                    Log.d(CLASS_NAME, "Updating WebView parameters via Javascript");
                    view.loadUrl("javascript:(function() { " +
                            // this will remove the Google Search Result entirely
                            //"document.getElementById('gsr').style.display = 'none'; " +
                            // this removes the Google Search Bar and stuff
                            "document.getElementById('hdtb').style.display = 'none'; " +
                            "document.getElementById('sfcnt').style.display = 'none'; " +
                            "document.getElementById('qslc').style.display = 'none'; " +
                            "})()");
                }
            }
        });
        webView.setEnabled(false);
        webView.setVisibility(View.GONE);

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
            ToolTipGenerator.showTooltip(getContext(), animationPanel, getString(R.string.write_on_the_slate), Tooltip.Gravity.CENTER);
            isFirstTimeGesture = false;
        }

        int currPos = txtToDisplay.length();

        if (mCallback.isLessonRunning() && (currPos < exerciseToDisplay.length())) {
            for (int j = 0; j < predictions.size(); j++) {
                String prediction = String.valueOf(predictions.get(j).name);
                String target = String.valueOf(exerciseToDisplay.charAt(currPos));

                if (predictions.get(j).score > 1.5) {
                    if (prediction.equals(target)) {
                        txtToDisplay += prediction;
                        gTextView.setText(txtToDisplay);
                        break;
                    }
                }

                // TODO: think of some way to add a value to a score if it is correct and below the threshold
                //Log.d(CLASS_NAME, String.valueOf(predictions.get(j).score));
            }
        } else {
            if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
                txtToDisplay += predictions.get(0).name;
                //Toast.makeText(this, txtToDisplay, Toast.LENGTH_SHORT).show();
                gTextView.setText(txtToDisplay);
            }

        }

        // don't worry about isFirstTimeGesture for abc slate
        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
            ToolTipGenerator.showTooltip(this.getContext(), speakButton, getString(R.string.hear_the_slate), Tooltip.Gravity.BOTTOM);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clearAbc:
                clearSlate();

                if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
                    if (mCallback.isLessonRunning()) {
                        ToolTipGenerator.showTooltip(this.getContext(), speakButton, getString(R.string.hear_the_slate), Tooltip.Gravity.BOTTOM);
                    }
                    toolTipGuard = false;
                }

                break;
            case R.id.speakAbc:
                if (mCallback.isLessonRunning() && !exerciseToDisplay.equals("")) {
                    mCallback.clearSticker();

                    if (mCallback.getSettings().isSwahiliOn()) {
                        mCallback.speakPhonetic(exerciseToDisplay, phoneticToPronouce, false);
                    } else {
                        mCallback.speakText(exerciseToDisplay, false);
                    }

                    if (mCallback.getSettings().isLearningMode()) {
                        drawText.setText(exerciseToDisplay, Color.WHITE);
                    }
                } else {
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
    private final TextWatcher txtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = (String) s.toString();
            //Log.d(CLASS_NAME, "Comparing text: " + text);

            // checking if there is an exercise and the slate match
            if (exerciseToDisplay.equals(text) && !text.equals("") &&
                    mCallback.isLessonRunning()) {
                // this is where we can increment score for lessons
                Toast.makeText(getActivity(), getString(R.string.match), Toast.LENGTH_SHORT).show();
                mCallback.animateSticker(Constants.StickerType.IMAGE);
                if (mCallback.getLessonThread() != null) {
                    if(mCallback.getSettings().isTestingMode()) {
                        mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_INCREMENT_SCORE_5);
                    }
                    mCallback.getLessonThread().sendMessage(MessagingConstants.MSG_POKE_THREAD);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    @Override
    public void onResume() {
        super.onResume();
        webView.resumeTimers();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.pauseTimers();
    }
}
