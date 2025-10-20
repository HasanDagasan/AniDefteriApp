package com.hasandagasan.anidefteri.classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class HeartButton extends AppCompatButton {

    private Paint paint;
    private int heartColor = 0xFFFF0000;

    public HeartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(heartColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();

        float centerX = width / 2;
        float centerY = height / 2;

        Path path = new Path();
        path.moveTo(centerX, height * 0.95f);

        // Sol üst kavis
        path.cubicTo(centerX - (width / 1.5f), height * 0.85f,
                centerX - (width / 2f), height * 0.01f,
                centerX, centerY);

        // Sağ üst kavis
        path.cubicTo(centerX + (width / 2f), height * 0.01f,
                centerX + (width / 1.5f), height * 0.85f,
                centerX, height * 0.95f);

        canvas.drawPath(path, paint);

        super.onDraw(canvas);
    }

    public void setHeartColor(int color) {
        heartColor = color;
        paint.setColor(heartColor);
        invalidate();
    }
}