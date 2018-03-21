package com.edtech.animations;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.edtech.R;

/**
 * Created by gautamkarnik on 2015-05-25.
 */
public class MathPanel extends AnimationPanel {

    private final String CLASS_NAME = getClass().getSimpleName();

    public MathPanel(Context context) {
        super(context);
    }

    public MathPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MathPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(getResources().getColor(R.color.slate_blue));
            if (blob != null) {
                blob.draw(canvas);
            }
            if (text != null) {
                text.draw(canvas);
            }
        }
    }
}
