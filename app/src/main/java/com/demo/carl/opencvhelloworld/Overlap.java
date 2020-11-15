package com.demo.carl.opencvhelloworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class Overlap extends View {
    private RectF mBox;
    private RectF mMaskBox;
    private Paint mPaint;
    private Paint mMaskPaint;
    public Overlap(Context context) {
        this(context,null);
    }

    public Overlap(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public Overlap(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBox = new RectF();
        initPaint();
    }
    public void setMaskBox(RectF box){
        this.mMaskBox = box;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mMaskBox,mMaskPaint);
        if (mBox!=null)
            canvas.drawRect(mBox,mPaint);
    }
    private void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(7);
        mPaint.setStyle(Paint.Style.STROKE);

        mMaskPaint = new Paint();
        mMaskPaint.setColor(Color.RED);
        mMaskPaint.setStrokeWidth(5);
        mMaskPaint.setStyle(Paint.Style.STROKE);
    }
    public void drawBox(RectF box){
        log("into drawBox.");
        mBox = box;
        invalidate();
    }
    private void log(String str){
        Log.i("carl",str);
    }
}
