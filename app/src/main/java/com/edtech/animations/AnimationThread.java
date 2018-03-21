package com.edtech.animations;

// Based on source - http://obviam.net/index.php/a-very-basic-the-game-loop-for-android

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by gautamkarnik on 2015-05-21.
 */
public class AnimationThread extends Thread {

    private final String CLASS_NAME = getClass().getSimpleName();

    // flag to hold animation state
    private boolean isRunning;
    private SurfaceHolder surfaceHolder;
    private AnimationPanel animationPanel;

    public AnimationThread(SurfaceHolder surfaceHolder, AnimationPanel animationPanel) {
        super();
        this.animationPanel = animationPanel;
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean running){
        this.isRunning = running;
    }

    @Override
    public void run() {
        long tickCount = 0;
        Log.d(CLASS_NAME, "Start animation loop");
        while (isRunning) {
            tickCount++;
            // update animation state
            animationPanel.update();

            // render state to the screen
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            Canvas c = null;
            try {
                c = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    animationPanel.draw(c);
                }

            } finally {
                if (c != null) {
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
        Log.d(CLASS_NAME, "Animation loop executed " + tickCount + " times.");
    }
}
