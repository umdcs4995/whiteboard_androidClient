package com.umdcs4995.whiteboard.uiElements;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom popup to choose the size of the brush.
 */
public class ChooseBrushSizeCircle extends View {
    private int radius;
    private Paint paint;
    private int color;

    public ChooseBrushSizeCircle(Context context) {
        super(context);
        paint = new Paint();
    }

    public ChooseBrushSizeCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    public ChooseBrushSizeCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
    }

    private void init(Context context) {
        //do stuff that was in your original constructor...
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        // RectF rect = new RectF(100, 100, 200, 200);
        // canvas.drawRect(rect, paint);

        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, getRadius(), paint);
        //canvas.drawLine(10, 10, 10, 10 + 15, paint);

    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * @return the radius
     */
    public int getRadius() {
        return radius;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
