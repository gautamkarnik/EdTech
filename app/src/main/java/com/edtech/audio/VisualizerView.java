package com.edtech.audio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import com.edtech.R;

// Source: http://www.vogella.com/code/ApiDemos/src/com/example/android/apis/media/AudioFxDemo.html

/**
 * A simple class that draws waveform data received from a
 * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
 */
public class VisualizerView extends View {
    private final String CLASS_NAME = getClass().getSimpleName();
    protected byte[] mBytes;
    protected float[] mPoints;
    protected Rect mRect = new Rect();
    protected Paint mForePaint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(getResources().getColor(R.color.slate_audio));

    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(getResources().getColor(R.color.slate_audio_background));
        cycleColor();
        render(canvas);
    }

    protected void render(Canvas canvas) {
        if (mBytes == null) {
            return;
        }

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        mRect.set(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2
                    + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2
                    + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }

        // need to split points in half for some devices
        canvas.drawLines(mPoints, 0, mPoints.length/2, mForePaint);
        canvas.drawLines(mPoints, mPoints.length/2, mPoints.length/2, mForePaint);
    }

    private static float colorCounter = 0;
    private boolean cycleColor = false;

    public void setCycleColor(boolean cycleColor) {
        this.cycleColor = cycleColor;
    }

    protected void cycleColor()
    {
        if (cycleColor) {
            int r = (int)Math.floor(128*(Math.sin(colorCounter) + 1));
            int g = (int)Math.floor(128*(Math.sin(colorCounter + 2) + 1));
            int b = (int)Math.floor(128*(Math.sin(colorCounter + 4) + 1));
            mForePaint.setColor(Color.argb(255, r, g, b));
            colorCounter += 0.03;
        } else {
            mForePaint.setColor(getResources().getColor(R.color.slate_audio));
        }
    }

}