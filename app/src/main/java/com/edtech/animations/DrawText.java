package com.edtech.animations;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by gautamkarnik on 2015-05-21.
 */
public class DrawText extends View {

    private final String CLASS_NAME = getClass().getSimpleName();
    public float x, y;
    private Bitmap imageText;
    private int hashCode;

    public DrawText(Context context) {
        super(context);
        x = y = 0;
    }

    public void setText(String text, int textColor) {
        if (!text.equals("") || (hashCode != text.hashCode())){
            hashCode = text.hashCode();
            float textSize = calcSizeInDip(text);
            imageText = textAsBitmap(text, DrawText.getPixelsFromDip(textSize), textColor);
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageText != null) {
            x = canvas.getWidth() / 2 - imageText.getWidth() / 2 ;
            y = canvas.getHeight() / 2 - imageText.getHeight() / 2;
            canvas.drawBitmap(imageText, x, y, null);
        }
    }

    //Source: http://stackoverflow.com/questions/11720093/which-unit-of-measurement-does-the-paint-settextsizefloat-use
    public static float getPixelsFromDip(float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, Resources.getSystem().getDisplayMetrics());
    }

    public static float getDipFromPixels(float px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().getDisplayMetrics());
    }

    // Source: http://stackoverflow.com/quaestions/8799290/convert-string-text-to-bitmap
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {

        Bitmap image = null;

        if (!text.equals("")) {
            Paint paint = new Paint();
            paint.setTextSize(getPixelsFromDip(textSize));
            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.LEFT);
            int width = (int) (paint.measureText(text) + 0.5f); // round
            float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is nagative
            int height = (int) (baseline + paint.descent() + 0.5f);
            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawText(text, 0, baseline, paint);

        }
        return image;
    }

    public float calcSizeInDip(String text) {
        float size = 0;
        if (!text.equals("")) {
            int chars = text.length();
            float pxPerChar = 0;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                float widthDpi = getResources().getDisplayMetrics().xdpi;
                float widthPx = getResources().getDisplayMetrics().widthPixels;

                pxPerChar = widthPx / chars;

                // an older calculation
                //float charDensity = widthDpi / chars;
                //float size = charDensity / getResources().getDisplayMetrics().scaledDensity;
            } else {

                // keeping this way because we may want to change this calculation for height
                float heightDpi = getResources().getDisplayMetrics().ydpi;
                float heightPx = getResources().getDisplayMetrics().heightPixels;

                pxPerChar = (chars > 1) ? heightPx / chars : heightPx / 2;
            }

            float pixelsNeeded = pxPerChar / getResources().getDisplayMetrics().scaledDensity;
            size = pixelsNeeded / getResources().getDisplayMetrics().density;
        }
        return size;
    }
}
