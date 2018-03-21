package com.edtech.activities;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by gautamkarnik on 2015-04-18.
 */
public class CustomViewPager extends ViewPager {

    private final String CLASS_NAME = getClass().getSimpleName();

    private boolean mEnabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

}
