package com.edtech.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by gautamkarnik on 2015-05-21.
 */
public class DrawBlob extends View {

    private final String CLASS_NAME = getClass().getSimpleName();
    public float x, y;
    private Bitmap blob;

    public DrawBlob(Context context) {
        super(context);
        x = y = 0;
    }

    public DrawBlob(Context context, Bitmap blob) {
        super(context);
        this.blob = blob;
    }

    public void setBlob(Bitmap blob) {
        this.blob = blob;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(blob, x, y, null);
    }
}
