package com.edtech.animations;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.edtech.R;

// Based on source - http://obviam.net/index.php/a-very-basic-the-game-loop-for-android
/**
 * Created by gautamkarnik on 2015-05-21.
 */

public class AnimationPanel extends SurfaceView implements SurfaceHolder.Callback {

    private final String CLASS_NAME = getClass().getSimpleName();
    private AnimationThread thread;
    private SurfaceHolder holder;

    public DrawBlob blob;
    public DrawText text;

    public AnimationPanel(Context context) {
        super(context);
        init();
    }

    public AnimationPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // adding the callback (this) to the surface holder to intercept events
        holder = getHolder();
        holder.addCallback(this);

        // make the AnimationPanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // create the animation loop thread
        thread = new AnimationThread(holder, this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // tell the thread to shut down and wait for it to finish
        // this is a clean shutdown
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);
        // get the index of the pointer associated with the action.
        int index = MotionEventCompat.getActionIndex(event);
        int xPos = 0;
        int yPos = 0;

        if (event.getPointerCount() > 1 ) {
            //Log.d(CLASS_NAME, "Multi touch event");
            // The coordinates of the current screen contact, relative to
            // the responding View
            xPos = (int) MotionEventCompat.getX(event, index);
            yPos = (int) MotionEventCompat.getY(event, index);
        } else {
            //Log.d(CLASS_NAME, "Single touch event");
            xPos = (int) MotionEventCompat.getX(event, index);
            yPos = (int) MotionEventCompat.getY(event, index);
        }

        //Log.d(CLASS_NAME, "Coords: x=" + xPos + ",y=" + yPos);

        String actionString;

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                actionString = "DOWN";
                break;
            case MotionEvent.ACTION_UP:
                actionString = "UP";
                break;
            case MotionEvent.ACTION_MOVE:
                actionString = "MOVE";
                if (blob != null) {
                    blob.x = xPos;
                    blob.y = yPos;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                actionString = "POINTER_DOWN";
                break;
            case MotionEvent.ACTION_POINTER_UP:
                actionString = "POINTER_UP";
                break;
            default:
                actionString = "";
                break;
        }

        //Log.d(CLASS_NAME, "The action is " + actionString);

        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            super.draw(canvas);
            canvas.drawColor(getResources().getColor(R.color.slate_green));
            if (blob != null) {
                blob.draw(canvas);
            }
            if (text != null) {
                text.draw(canvas);
            }
        }
    }

    public void update() {
        // slow the thread down
        try {
            Thread.sleep(50); // update 20 times a second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setDrawBlob(DrawBlob blob) {
        this.blob = blob;
    }

    public void setDrawText(DrawText text) {
        this.text = text;
    }
}
