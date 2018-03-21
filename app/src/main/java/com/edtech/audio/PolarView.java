package com.edtech.audio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.edtech.R;

/**
 * Created by gautamkarnik on 2016-01-22.
 * Based on source: https://github.com/felixpalmer/android-visualizer/blob/master/src/com/pheelicks/visualizer/renderer/CircleRenderer.java
 */
public class PolarView extends VisualizerView {
    private final String CLASS_NAME = getClass().getSimpleName();
    private float[] cartPoint = new float[2];
    private float[] polarPoint = new float[2];

    public PolarView(Context context) {
        super(context);
        init();
    }

    public PolarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PolarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void init() {
        super.init();

        mForePaint.setStrokeWidth(5f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(getResources().getColor(R.color.slate_audio));

        modulation = 0;
    }

    float modulation = 0;
    float aggresive = 0.33f;
    private void toPolar(float[] cartesian, Rect rect)
    {
        double cX = rect.width()/2;
        double cY = rect.height()/2;
        double angle = (cartesian[0]) * 2 * Math.PI;
        double radius = ((rect.width()/2) * (1 - aggresive) + aggresive * cartesian[1]/2) * (1.2 + Math.sin(modulation))/2.2;
        polarPoint[0] = (float)(cX + radius * Math.sin(angle));
        polarPoint[1] = (float)(cY + radius * Math.cos(angle));
    }

    @Override
    protected void render(Canvas canvas) {
        if (mBytes == null) {
            return;
        }

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        mRect.set(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < mBytes.length - 1; i++) {
            // point 1
            cartPoint[0] = (float) i / (mBytes.length - 1);
            cartPoint[1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;

            toPolar(cartPoint, mRect);
            mPoints[i * 4] = polarPoint[0];
            mPoints[i * 4 + 1] = polarPoint[1];

            //point 2
            cartPoint[0] = (float)(i + 1) / (mBytes.length - 1);
            cartPoint[1] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;

            toPolar(cartPoint, mRect);
            mPoints[i * 4 + 2] = polarPoint[0];
            mPoints[i * 4 + 3] = polarPoint[1];
        }

        // need to split points in half for some devices
        canvas.drawLines(mPoints, 0, mPoints.length/2, mForePaint);
        canvas.drawLines(mPoints, mPoints.length/2, mPoints.length/2, mForePaint);

        // Controls the pulsing rate
        modulation += 0.04;
    }
}
