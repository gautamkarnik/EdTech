package com.edtech.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.edtech.application.Settings;

/**
 * Created by gautamkarnik on 2015-07-10.
 */
public class ProfileNameEditText extends EditText implements View.OnTouchListener, View.OnFocusChangeListener, TextView.OnEditorActionListener {

    private final String CLASS_NAME = getClass().getSimpleName();
    private Settings mSettings = null;

    public ProfileNameEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfileNameEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProfileNameEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ProfileNameEditText(Context context) {
        super(context);
        init();
    }


    private void init() {
        setOnTouchListener(this);
        setOnFocusChangeListener(this);
        setOnEditorActionListener(this);
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(CLASS_NAME, "Keyback");
            setFocusable(false);
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setText("", TextView.BufferType.EDITABLE);
        } else {
            setText(mSettings.getProfileName(), TextView.BufferType.NORMAL);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT) {
            String name = v.getText().toString();
            Log.d(CLASS_NAME, "Name: " + name);
            mSettings.setProfileName(name);
            v.setFocusable(false);
        }
        return false;
    }
}
