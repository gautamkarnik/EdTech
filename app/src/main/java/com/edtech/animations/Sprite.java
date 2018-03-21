package com.edtech.animations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by gautamkarnik on 2015-05-21.
 */
public class Sprite {

    private final String CLASS_NAME = getClass().getSimpleName();

    private int x, y;
    private int xSpeed, ySpeed;
    private int height, width;
    private Bitmap blob;
    private View view;
    private int currentFrame = 0;
    private int currentPosition = 0;
    private int direction = 0;
    private int rows = 1;
    private int columns = 1;

    public Sprite(View view, Bitmap blob, int rows, int columns) {
        this.blob = blob;
        this.view = view;
        this.rows = rows;
        this.columns = columns;
        // 1 rows
        height = this.blob.getHeight() / rows;
        // 1 columns
        width = this.blob.getWidth() / columns;
        x = y = 0;
        // moving to the right
        xSpeed = 5;
        ySpeed = 0;
    }

    public void onDraw(Canvas canvas)  {
        try {
            update();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int srcX = currentFrame * width;
        int srcY = currentPosition * height;
        Rect src = new Rect(srcX, srcY, srcX+width, srcY+height);
        Rect dst = new Rect(x, y, x+width, y+height);
        canvas.drawBitmap(blob, src, dst, null);
    }

    private void update() throws InterruptedException {

        // depends on the bitmap
        // 0 = down
        // 1 = left
        // 2 = right
        // 3 = up

        // facing down
        if (x > view.getWidth() - width - xSpeed) {
            xSpeed = 0;
            ySpeed = 5;
            direction = 0;
        }
        // going left
        if (y > view.getHeight() - height - ySpeed) {
            xSpeed = -5;
            ySpeed = 0;
            direction = 1;
        }
        // going right
        if (y + ySpeed < 0) {
            y = 0;
            xSpeed = 5;
            ySpeed = 0;
            direction = 2;
        }
        // facing up
        if (x + xSpeed < 0) {
            x = 0;
            xSpeed = 0;
            ySpeed = -5;
            direction = 3;
        }


        try {
            Thread.sleep(10); // update 100 times a second (1000ms / time sleeping)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        currentFrame = ++currentFrame % rows;
        currentPosition = direction % columns;
        x += xSpeed;
        y += ySpeed;
    }

}
